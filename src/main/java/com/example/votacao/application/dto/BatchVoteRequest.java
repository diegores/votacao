package com.example.votacao.application.dto;

import com.example.votacao.domain.model.VoteType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BatchVoteRequest {
    
    @NotNull(message = "Member ID is required")
    private UUID memberId;
    
    @NotNull(message = "Vote type is required")
    private VoteType voteType;
}