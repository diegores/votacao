package com.example.votacao.domain.exception;

/**
 * Base exception for business rule violations.
 * 
 * This exception should be used when business logic validation fails,
 * such as attempting to vote on a closed session or duplicate voting.
 */
public class BusinessException extends RuntimeException {
    
    public BusinessException(String message) {
        super(message);
    }
    
    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }
}