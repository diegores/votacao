package com.example.votacao.infrastructure.web;

import com.example.votacao.domain.exception.BusinessException;
import com.example.votacao.domain.exception.EntityNotFoundException;
import com.example.votacao.domain.exception.VotingException;
import com.example.votacao.infrastructure.external.CpfValidationClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Global exception handler for the application.
 * 
 * Provides centralized exception handling with proper HTTP status codes
 * and consistent error response format. This improves API usability and
 * debugging capabilities.
 * 
 * Design Decision: Centralized exception handling reduces code duplication
 * and ensures consistent error responses across all endpoints.
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    
    /**
     * Handles entity not found exceptions.
     */
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleEntityNotFound(EntityNotFoundException ex) {
        log.warn("Entity not found: {}", ex.getMessage());
        
        ErrorResponse error = new ErrorResponse(
                "ENTITY_NOT_FOUND",
                ex.getMessage(),
                LocalDateTime.now()
        );
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }
    
    /**
     * Handles voting business rule violations.
     */
    @ExceptionHandler(VotingException.class)
    public ResponseEntity<ErrorResponse> handleVotingException(VotingException ex) {
        log.warn("Voting rule violation: {}", ex.getMessage());
        
        ErrorResponse error = new ErrorResponse(
                "VOTING_ERROR",
                ex.getMessage(),
                LocalDateTime.now()
        );
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
    
    /**
     * Handles general business exceptions.
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException ex) {
        log.warn("Business rule violation: {}", ex.getMessage());

        ErrorResponse error = new ErrorResponse(
                "BUSINESS_ERROR",
                ex.getMessage(),
                LocalDateTime.now()
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    /**
     * Handles CPF unable to vote exceptions.
     */
    @ExceptionHandler(CpfValidationClient.CpfUnableToVoteException.class)
    public ResponseEntity<ErrorResponse> handleCpfUnableToVote(CpfValidationClient.CpfUnableToVoteException ex) {
        log.warn("CPF unable to vote: {}", ex.getMessage());

        ErrorResponse error = new ErrorResponse(
                "CPF_UNABLE_TO_VOTE",
                ex.getMessage(),
                LocalDateTime.now()
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    /**
     * Handles CPF not found exceptions.
     */
    @ExceptionHandler(CpfValidationClient.CpfNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleCpfNotFound(CpfValidationClient.CpfNotFoundException ex) {
        log.warn("CPF not found: {}", ex.getMessage());

        ErrorResponse error = new ErrorResponse(
                "CPF_NOT_FOUND",
                ex.getMessage(),
                LocalDateTime.now()
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
    
    /**
     * Handles validation errors from request body validation.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException ex) {
        log.warn("Validation error: {}", ex.getMessage());
        
        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            fieldErrors.put(fieldName, errorMessage);
        });
        
        ErrorResponse error = new ErrorResponse(
                "VALIDATION_ERROR",
                "Invalid request data",
                LocalDateTime.now(),
                fieldErrors
        );
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
    
    /**
     * Handles illegal argument exceptions.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
        log.warn("Invalid argument: {}", ex.getMessage());
        
        ErrorResponse error = new ErrorResponse(
                "INVALID_ARGUMENT",
                ex.getMessage(),
                LocalDateTime.now()
        );
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
    
    /**
     * Handles illegal state exceptions.
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalState(IllegalStateException ex) {
        log.warn("Invalid state: {}", ex.getMessage());
        
        ErrorResponse error = new ErrorResponse(
                "INVALID_STATE",
                ex.getMessage(),
                LocalDateTime.now()
        );
        
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }
    
    /**
     * Handles unexpected exceptions.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        log.error("Unexpected error occurred", ex);
        
        ErrorResponse error = new ErrorResponse(
                "INTERNAL_ERROR",
                "An unexpected error occurred. Please try again later.",
                LocalDateTime.now()
        );
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
    
    /**
     * Standard error response format.
     */
    public static class ErrorResponse {
        private final String code;
        private final String message;
        private final LocalDateTime timestamp;
        private final Map<String, String> details;
        
        public ErrorResponse(String code, String message, LocalDateTime timestamp) {
            this.code = code;
            this.message = message;
            this.timestamp = timestamp;
            this.details = new HashMap<>();
        }
        
        public ErrorResponse(String code, String message, LocalDateTime timestamp, Map<String, String> details) {
            this.code = code;
            this.message = message;
            this.timestamp = timestamp;
            this.details = details != null ? details : new HashMap<>();
        }
        
        // Getters
        public String getCode() { return code; }
        public String getMessage() { return message; }
        public LocalDateTime getTimestamp() { return timestamp; }
        public Map<String, String> getDetails() { return details; }
    }
}