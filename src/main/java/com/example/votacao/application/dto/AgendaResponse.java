package com.example.votacao.application.dto;

import com.example.votacao.domain.model.VotingSessionStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AgendaResponse {
    private UUID id;
    private String title;
    private String description;
    private LocalDateTime createdAt;
    private VotingSessionStatus status;
    private LocalDateTime sessionStartTime;
    private LocalDateTime sessionEndTime;
    private boolean votingOpen;
}