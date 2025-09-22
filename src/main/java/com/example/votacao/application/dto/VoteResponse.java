package com.example.votacao.application.dto;

import com.example.votacao.domain.model.VoteType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VoteResponse {
    private UUID id;
    private UUID memberId;
    private VoteType voteType;
    private LocalDateTime votedAt;
}