package com.example.votacao.infrastructure.web;

import com.example.votacao.domain.exception.EntityNotFoundException;
import com.example.votacao.domain.exception.VotingException;
import com.example.votacao.infrastructure.external.CpfValidationClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for GlobalExceptionHandler.
 * 
 * Verifies that all exception types are properly handled with
 * correct HTTP status codes and error response formats.
 */
@DisplayName("GlobalExceptionHandler Tests")
class GlobalExceptionHandlerTest {
    
    private GlobalExceptionHandler exceptionHandler;
    
    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
    }
    
    @Test
    @DisplayName("Should handle EntityNotFoundException with 404 status")
    void shouldHandleEntityNotFoundException() {
        // Given
        EntityNotFoundException exception = new EntityNotFoundException("Agenda", UUID.randomUUID());
        
        // When
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = 
                exceptionHandler.handleEntityNotFound(exception);
        
        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo("ENTITY_NOT_FOUND");
        assertThat(response.getBody().getMessage()).contains("Agenda not found");
        assertThat(response.getBody().getTimestamp()).isNotNull();
    }
    
    @Test
    @DisplayName("Should handle VotingException with 400 status")
    void shouldHandleVotingException() {
        // Given
        VotingException exception = new VotingException("Member has already voted");
        
        // When
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = 
                exceptionHandler.handleVotingException(exception);
        
        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo("VOTING_ERROR");
        assertThat(response.getBody().getMessage()).isEqualTo("Member has already voted");
    }
    
    @Test
    @DisplayName("Should handle validation errors with field details")
    void shouldHandleValidationErrors() {
        // Given
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError = new FieldError("request", "cpf", "CPF is required");
        
        when(exception.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getAllErrors()).thenReturn(List.of(fieldError));
        
        // When
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = 
                exceptionHandler.handleValidationException(exception);
        
        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo("VALIDATION_ERROR");
        assertThat(response.getBody().getDetails()).containsEntry("cpf", "CPF is required");
    }
    
    @Test
    @DisplayName("Should handle IllegalArgumentException with 400 status")
    void shouldHandleIllegalArgumentException() {
        // Given
        IllegalArgumentException exception = new IllegalArgumentException("Invalid member ID");
        
        // When
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = 
                exceptionHandler.handleIllegalArgument(exception);
        
        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo("INVALID_ARGUMENT");
        assertThat(response.getBody().getMessage()).isEqualTo("Invalid member ID");
    }
    
    @Test
    @DisplayName("Should handle IllegalStateException with 409 status")
    void shouldHandleIllegalStateException() {
        // Given
        IllegalStateException exception = new IllegalStateException("Voting session is closed");
        
        // When
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = 
                exceptionHandler.handleIllegalState(exception);
        
        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo("INVALID_STATE");
        assertThat(response.getBody().getMessage()).isEqualTo("Voting session is closed");
    }
    
    @Test
    @DisplayName("Should handle CpfUnableToVoteException with 400 status")
    void shouldHandleCpfUnableToVoteException() {
        // Given
        CpfValidationClient.CpfUnableToVoteException exception =
                new CpfValidationClient.CpfUnableToVoteException("CPF is unable to vote: 123.***.**-45");

        // When
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response =
                exceptionHandler.handleCpfUnableToVote(exception);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo("CPF_UNABLE_TO_VOTE");
        assertThat(response.getBody().getMessage()).isEqualTo("CPF is unable to vote: 123.***.**-45");
        assertThat(response.getBody().getTimestamp()).isNotNull();
    }

    @Test
    @DisplayName("Should handle CpfNotFoundException with 400 status")
    void shouldHandleCpfNotFoundException() {
        // Given
        CpfValidationClient.CpfNotFoundException exception =
                new CpfValidationClient.CpfNotFoundException("CPF not found: 123.***.**-45");

        // When
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response =
                exceptionHandler.handleCpfNotFound(exception);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo("CPF_NOT_FOUND");
        assertThat(response.getBody().getMessage()).isEqualTo("CPF not found: 123.***.**-45");
        assertThat(response.getBody().getTimestamp()).isNotNull();
    }

    @Test
    @DisplayName("Should handle generic exceptions with 500 status")
    void shouldHandleGenericException() {
        // Given
        RuntimeException exception = new RuntimeException("Unexpected database error");

        // When
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response =
                exceptionHandler.handleGenericException(exception);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo("INTERNAL_ERROR");
        assertThat(response.getBody().getMessage()).isEqualTo("An unexpected error occurred. Please try again later.");
    }
}