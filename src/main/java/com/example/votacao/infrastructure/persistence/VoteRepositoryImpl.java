package com.example.votacao.infrastructure.persistence;

import com.example.votacao.domain.model.Vote;
import com.example.votacao.domain.repository.VoteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class VoteRepositoryImpl implements VoteRepository {
    
    private final JpaVoteRepository jpaRepository;
    
    @Override
    public Vote save(Vote vote) {
        return jpaRepository.save(vote);
    }
    
    @Override
    public List<Vote> saveAll(List<Vote> votes) {
        return jpaRepository.saveAll(votes);
    }
    
    @Override
    public Optional<Vote> findById(UUID id) {
        return jpaRepository.findById(id);
    }
    
    @Override
    public List<Vote> findByAgendaId(UUID agendaId) {
        return jpaRepository.findByAgendaId(agendaId);
    }
    
    @Override
    public List<Vote> findByMemberId(UUID memberId) {
        return jpaRepository.findByMemberId(memberId);
    }
    
    @Override
    public Optional<Vote> findByAgendaIdAndMemberId(UUID agendaId, UUID memberId) {
        return jpaRepository.findByAgendaIdAndMemberId(agendaId, memberId);
    }
    
    @Override
    public void delete(Vote vote) {
        jpaRepository.delete(vote);
    }
    
    @Override
    public boolean existsByAgendaIdAndMemberId(UUID agendaId, UUID memberId) {
        return jpaRepository.existsByAgendaIdAndMemberId(agendaId, memberId);
    }
}