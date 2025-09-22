package com.example.votacao.infrastructure.persistence;

import com.example.votacao.domain.model.Vote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface JpaVoteRepository extends JpaRepository<Vote, UUID> {
    List<Vote> findByAgendaId(UUID agendaId);
    List<Vote> findByMemberId(UUID memberId);
    Optional<Vote> findByAgendaIdAndMemberId(UUID agendaId, UUID memberId);
    boolean existsByAgendaIdAndMemberId(UUID agendaId, UUID memberId);
}