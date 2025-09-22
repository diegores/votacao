package com.example.votacao.domain.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class VotingResult {
    private long yesVotes;
    private long noVotes;
    private long totalVotes;
    
    public String getResult() {
        if (yesVotes > noVotes) {
            return "APPROVED";
        } else if (noVotes > yesVotes) {
            return "REJECTED";
        } else {
            return "TIED";
        }
    }
    
    public double getYesPercentage() {
        return totalVotes > 0 ? (double) yesVotes / totalVotes * 100 : 0;
    }
    
    public double getNoPercentage() {
        return totalVotes > 0 ? (double) noVotes / totalVotes * 100 : 0;
    }
}