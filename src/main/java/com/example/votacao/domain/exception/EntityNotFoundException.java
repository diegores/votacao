package com.example.votacao.domain.exception;

/**
 * Exception thrown when a requested entity is not found.
 * 
 * This provides more specific error handling than generic IllegalArgumentException
 * and allows for better HTTP status code mapping.
 */
public class EntityNotFoundException extends RuntimeException {
    
    public EntityNotFoundException(String entityType, Object id) {
        super(String.format("%s not found with id: %s", entityType, id));
    }
    
    public EntityNotFoundException(String message) {
        super(message);
    }
}