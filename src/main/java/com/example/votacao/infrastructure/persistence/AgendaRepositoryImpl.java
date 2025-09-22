package com.example.votacao.infrastructure.persistence;

import com.example.votacao.domain.model.Agenda;
import com.example.votacao.domain.model.VotingSessionStatus;
import com.example.votacao.domain.repository.AgendaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AgendaRepositoryImpl implements AgendaRepository {
    
    private final JpaAgendaRepository jpaRepository;
    
    @Override
    public Agenda save(Agenda agenda) {
        return jpaRepository.save(agenda);
    }
    
    @Override
    public Optional<Agenda> findById(UUID id) {
        return jpaRepository.findById(id);
    }
    
    @Override
    public List<Agenda> findAll() {
        return jpaRepository.findAll();
    }
    
    @Override
    public List<Agenda> findByStatus(VotingSessionStatus status) {
        return jpaRepository.findByStatus(status);
    }
    
    @Override
    public void delete(Agenda agenda) {
        jpaRepository.delete(agenda);
    }
    
    @Override
    public boolean existsById(UUID id) {
        return jpaRepository.existsById(id);
    }
}