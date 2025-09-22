package com.example.votacao.domain.repository;

import com.example.votacao.domain.model.Vote;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface VoteRepository {
    Vote save(Vote vote);
    List<Vote> saveAll(List<Vote> votes);
    Optional<Vote> findById(UUID id);
    List<Vote> findByAgendaId(UUID agendaId);
    List<Vote> findByMemberId(UUID memberId);
    Optional<Vote> findByAgendaIdAndMemberId(UUID agendaId, UUID memberId);
    void delete(Vote vote);
    boolean existsByAgendaIdAndMemberId(UUID agendaId, UUID memberId);
}