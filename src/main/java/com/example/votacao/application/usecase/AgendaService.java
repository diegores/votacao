package com.example.votacao.application.usecase;

import com.example.votacao.application.dto.*;
import com.example.votacao.domain.model.Agenda;
import com.example.votacao.domain.model.VotingResult;
import com.example.votacao.domain.model.VotingSessionStatus;
import com.example.votacao.domain.repository.AgendaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class AgendaService {

    private static final String AGENDA_NOT_FOUND_WITH_ID = "Agenda not found with id:";
    private final AgendaRepository agendaRepository;
    
    public AgendaResponse createAgenda(CreateAgendaRequest request) {
        Agenda agenda = new Agenda(request.getTitle(), request.getDescription());
        Agenda savedAgenda = agendaRepository.save(agenda);
        return mapToResponse(savedAgenda);
    }
    
    public AgendaResponse openVotingSession(UUID agendaId, OpenVotingSessionRequest request) {
        Agenda agenda = agendaRepository.findById(agendaId)
                .orElseThrow(() -> new IllegalArgumentException(AGENDA_NOT_FOUND_WITH_ID +  agendaId));
        
        int duration = request.getDurationInMinutes() != null ? request.getDurationInMinutes() : 1;
        agenda.openVotingSession(duration);
        agenda.setStatus(VotingSessionStatus.OPEN);

        Agenda savedAgenda = agendaRepository.save(agenda);
        return mapToResponse(savedAgenda);
    }
    
    @Transactional(readOnly = true)
    @Cacheable(value = "agendas", key = "#agendaId")
    public AgendaResponse getAgenda(UUID agendaId) {
        Agenda agenda = agendaRepository.findById(agendaId)
                .orElseThrow(() -> new IllegalArgumentException(AGENDA_NOT_FOUND_WITH_ID + agendaId));
        return mapToResponse(agenda);
    }
    
    @Transactional(readOnly = true)
    public List<AgendaResponse> getAllAgendas() {
        return agendaRepository.findAll().stream()
                .map(this::mapToResponse)
                .toList();
    }
    
    @Transactional(readOnly = true)
    public List<AgendaResponse> getOpenVotingSessions() {
        return agendaRepository.findByStatus(VotingSessionStatus.OPEN).stream()
                .map(this::mapToResponse)
                .toList();
    }
    
    @Transactional(readOnly = true)
    @Cacheable(value = "voting-results", key = "#agendaId")
    public VotingResultResponse getVotingResult(UUID agendaId) {
        Agenda agenda = agendaRepository.findById(agendaId)
                .orElseThrow(() -> new IllegalArgumentException(AGENDA_NOT_FOUND_WITH_ID + " " + agendaId));
        
        VotingResult result = agenda.getVotingResult();
        return new VotingResultResponse(
                result.getYesVotes(),
                result.getNoVotes(),
                result.getTotalVotes(),
                result.getResult(),
                result.getYesPercentage(),
                result.getNoPercentage()
        );
    }
    
    public void closeExpiredVotingSessions() {
        List<Agenda> openAgendas = agendaRepository.findByStatus(VotingSessionStatus.OPEN);
        openAgendas.stream()
                .filter(agenda -> !agenda.isVotingOpen())
                .forEach(agenda -> {
                    agenda.closeVotingSession();
                    agendaRepository.save(agenda);
                });
    }
    
    private AgendaResponse mapToResponse(Agenda agenda) {
        return new AgendaResponse(
                agenda.getId(),
                agenda.getTitle(),
                agenda.getDescription(),
                agenda.getCreatedAt(),
                agenda.getStatus(),
                agenda.getSessionStartTime(),
                agenda.getSessionEndTime(),
                agenda.isVotingOpen()
        );
    }
}