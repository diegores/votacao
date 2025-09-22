package com.example.votacao.application.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VotingResultResponse {
    private long yesVotes;
    private long noVotes;
    private long totalVotes;
    private String result;
    private double yesPercentage;
    private double noPercentage;
}