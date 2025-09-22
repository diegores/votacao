package com.example.votacao.domain.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class VotingResultTest {
    
    @Test
    void shouldReturnApprovedWhenYesVotesWin() {
        VotingResult result = new VotingResult(3, 2, 5);
        assertEquals("APPROVED", result.getResult());
    }
    
    @Test
    void shouldReturnRejectedWhenNoVotesWin() {
        VotingResult result = new VotingResult(2, 3, 5);
        assertEquals("REJECTED", result.getResult());
    }
    
    @Test
    void shouldReturnTiedWhenVotesAreEqual() {
        VotingResult result = new VotingResult(2, 2, 4);
        assertEquals("TIED", result.getResult());
    }
    
    @Test
    void shouldCalculateCorrectPercentages() {
        VotingResult result = new VotingResult(3, 2, 5);
        
        assertEquals(60.0, result.getYesPercentage(), 0.01);
        assertEquals(40.0, result.getNoPercentage(), 0.01);
    }
    
    @Test
    void shouldHandleZeroVotes() {
        VotingResult result = new VotingResult(0, 0, 0);
        
        assertEquals(0.0, result.getYesPercentage());
        assertEquals(0.0, result.getNoPercentage());
        assertEquals("TIED", result.getResult());
    }
}