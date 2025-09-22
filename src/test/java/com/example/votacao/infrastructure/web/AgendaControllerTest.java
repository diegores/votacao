package com.example.votacao.infrastructure.web;

import com.example.votacao.application.dto.AgendaResponse;
import com.example.votacao.application.dto.CreateAgendaRequest;
import com.example.votacao.application.usecase.AgendaService;
import com.example.votacao.domain.model.VotingSessionStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AgendaControllerTest {
    
    @Mock
    private AgendaService agendaService;
    
    @InjectMocks
    private AgendaController agendaController;
    
    private AgendaResponse mockAgendaResponse;
    
    @BeforeEach
    void setUp() {
        mockAgendaResponse = new AgendaResponse(
                UUID.randomUUID(),
                "Test Agenda",
                "Test Description",
                LocalDateTime.now(),
                VotingSessionStatus.CLOSED,
                null,
                null,
                false
        );
    }
    
    @Test
    void shouldCreateAgenda() {
        // Given
        CreateAgendaRequest request = new CreateAgendaRequest("Test Agenda", "Description");
        when(agendaService.createAgenda(any(CreateAgendaRequest.class))).thenReturn(mockAgendaResponse);
        
        // When
        ResponseEntity<AgendaResponse> response = agendaController.createAgenda(request);
        
        // Then
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Test Agenda", response.getBody().getTitle());
    }
    
    @Test
    void shouldGetAllAgendas() {
        // Given
        List<AgendaResponse> agendas = Arrays.asList(mockAgendaResponse);
        when(agendaService.getAllAgendas()).thenReturn(agendas);
        
        // When
        ResponseEntity<List<AgendaResponse>> response = agendaController.getAllAgendas();
        
        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals("Test Agenda", response.getBody().get(0).getTitle());
    }
    
    @Test
    void shouldGetAgendaById() {
        // Given
        UUID agendaId = UUID.randomUUID();
        when(agendaService.getAgenda(agendaId)).thenReturn(mockAgendaResponse);
        
        // When
        ResponseEntity<AgendaResponse> response = agendaController.getAgenda(agendaId);
        
        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Test Agenda", response.getBody().getTitle());
    }
}