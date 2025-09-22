package com.example.votacao.application.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BatchVotingResponse {
    
    private int successfulVotes;
    private int failedVotes;
    private List<UUID> failedMemberIds;
    private String message;
    private long processingTimeMs;
    
    public static BatchVotingResponse success(int successfulVotes, long processingTimeMs) {
        return new BatchVotingResponse(
            successfulVotes, 
            0, 
            List.of(), 
            "All votes processed successfully", 
            processingTimeMs
        );
    }
    
    public static BatchVotingResponse partial(int successfulVotes, int failedVotes, 
                                            List<UUID> failedMemberIds, long processingTimeMs) {
        return new BatchVotingResponse(
            successfulVotes, 
            failedVotes, 
            failedMemberIds, 
            String.format("Processed %d successful votes, %d failed", successfulVotes, failedVotes),
            processingTimeMs
        );
    }
}