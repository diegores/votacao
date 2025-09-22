package com.example.votacao.domain.exception;

/**
 * Exception specific to voting operations.
 * 
 * Thrown when voting business rules are violated, such as:
 * - Attempting to vote on a closed session
 * - Duplicate voting attempts
 * - Invalid vote types
 */
public class VotingException extends BusinessException {
    
    public VotingException(String message) {
        super(message);
    }
    
    public VotingException(String message, Throwable cause) {
        super(message, cause);
    }
}