package com.example.votacao.infrastructure.persistence;

import com.example.votacao.domain.model.Agenda;
import com.example.votacao.domain.model.VotingSessionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface JpaAgendaRepository extends JpaRepository<Agenda, UUID> {
    List<Agenda> findByStatus(VotingSessionStatus status);
}