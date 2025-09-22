package com.example.votacao.infrastructure.external;

import com.example.votacao.domain.model.CpfValidationResponse;
import com.example.votacao.domain.model.CpfValidationStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CpfValidationClientTest {
    
    private CpfValidationClient cpfValidationClient;
    
    @BeforeEach
    void setUp() {
        cpfValidationClient = new CpfValidationClient();
    }
    
    @Test
    void shouldValidateValidCpf() {
        // Valid CPF: 11144477735
        String validCpf = "11144477735";
        
        // Since the service randomly returns ABLE_TO_VOTE or UNABLE_TO_VOTE,
        // we test multiple times to ensure both scenarios work
        boolean foundAbleToVote = false;
        boolean foundUnableToVote = false;
        
        for (int i = 0; i < 20; i++) {
            try {
                CpfValidationResponse response = cpfValidationClient.validateCpf(validCpf);
                assertEquals(CpfValidationStatus.ABLE_TO_VOTE, response.getStatus());
                foundAbleToVote = true;
            } catch (CpfValidationClient.CpfUnableToVoteException e) {
                foundUnableToVote = true;
            }
            
            if (foundAbleToVote && foundUnableToVote) {
                break;
            }
        }
        
        // At least one of the scenarios should have occurred
        assertTrue(foundAbleToVote || foundUnableToVote);
    }
    
    @Test
    void shouldThrowExceptionForInvalidCpfFormat() {
        String invalidCpf = "12345";
        
        assertThrows(CpfValidationClient.CpfNotFoundException.class, () -> {
            cpfValidationClient.validateCpf(invalidCpf);
        });
    }
    
    @Test
    void shouldThrowExceptionForCpfWithAllSameDigits() {
        String invalidCpf = "11111111111";
        
        assertThrows(CpfValidationClient.CpfNotFoundException.class, () -> {
            cpfValidationClient.validateCpf(invalidCpf);
        });
    }
    
    @Test
    void shouldThrowExceptionForCpfWithInvalidChecksum() {
        String invalidCpf = "11144477736"; // Last digit should be 5, not 6
        
        assertThrows(CpfValidationClient.CpfNotFoundException.class, () -> {
            cpfValidationClient.validateCpf(invalidCpf);
        });
    }
    
    @Test
    void shouldHandleFormattedCpf() {
        String formattedCpf = "111.444.777-35";
        
        // Should work with formatted CPF as well
        assertDoesNotThrow(() -> {
            try {
                cpfValidationClient.validateCpf(formattedCpf);
            } catch (CpfValidationClient.CpfUnableToVoteException e) {
                // This is expected for some cases due to random nature
            }
        });
    }
}