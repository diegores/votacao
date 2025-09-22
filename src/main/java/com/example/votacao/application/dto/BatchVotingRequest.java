package com.example.votacao.application.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BatchVotingRequest {
    
    @NotNull(message = "Agenda ID is required")
    private UUID agendaId;
    
    @NotEmpty(message = "Votes list cannot be empty")
    @Size(max = 10000, message = "Batch size cannot exceed 10,000 votes")
    @Valid
    private List<BatchVoteRequest> votes;
}