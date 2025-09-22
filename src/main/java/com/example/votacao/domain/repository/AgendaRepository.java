package com.example.votacao.domain.repository;

import com.example.votacao.domain.model.Agenda;
import com.example.votacao.domain.model.VotingSessionStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AgendaRepository {
    Agenda save(Agenda agenda);
    Optional<Agenda> findById(UUID id);
    List<Agenda> findAll();
    List<Agenda> findByStatus(VotingSessionStatus status);
    void delete(Agenda agenda);
    boolean existsById(UUID id);
}