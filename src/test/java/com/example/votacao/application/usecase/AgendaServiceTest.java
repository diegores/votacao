package com.example.votacao.application.usecase;

import com.example.votacao.application.dto.CreateAgendaRequest;
import com.example.votacao.application.dto.OpenVotingSessionRequest;
import com.example.votacao.application.dto.AgendaResponse;
import com.example.votacao.domain.model.Agenda;
import com.example.votacao.domain.model.VotingSessionStatus;
import com.example.votacao.domain.repository.AgendaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AgendaServiceTest {
    
    @Mock
    private AgendaRepository agendaRepository;
    
    @InjectMocks
    private AgendaService agendaService;
    
    private Agenda mockAgenda;
    private UUID agendaId;
    
    @BeforeEach
    void setUp() {
        agendaId = UUID.randomUUID();
        mockAgenda = new Agenda("Test Agenda", "Test Description");
        mockAgenda.setId(agendaId);
    }
    
    @Test
    void shouldCreateAgenda() {
        CreateAgendaRequest request = new CreateAgendaRequest("New Agenda", "Description");
        when(agendaRepository.save(any(Agenda.class))).thenReturn(mockAgenda);
        
        AgendaResponse response = agendaService.createAgenda(request);
        
        assertNotNull(response);
        assertEquals("Test Agenda", response.getTitle());
        assertEquals("Test Description", response.getDescription());
        assertEquals(VotingSessionStatus.CLOSED, response.getStatus());
        verify(agendaRepository).save(any(Agenda.class));
    }
    
    @Test
    void shouldOpenVotingSession() {
        OpenVotingSessionRequest request = new OpenVotingSessionRequest(5);
        when(agendaRepository.findById(agendaId)).thenReturn(Optional.of(mockAgenda));
        when(agendaRepository.save(any(Agenda.class))).thenReturn(mockAgenda);
        
        AgendaResponse response = agendaService.openVotingSession(agendaId, request);
        
        assertNotNull(response);
        verify(agendaRepository).findById(agendaId);
        verify(agendaRepository).save(mockAgenda);
    }
    
    @Test
    void shouldThrowExceptionWhenOpeningVotingSessionForNonExistentAgenda() {
        OpenVotingSessionRequest request = new OpenVotingSessionRequest(5);
        when(agendaRepository.findById(agendaId)).thenReturn(Optional.empty());
        
        assertThrows(IllegalArgumentException.class, () -> {
            agendaService.openVotingSession(agendaId, request);
        });
        
        verify(agendaRepository).findById(agendaId);
        verify(agendaRepository, never()).save(any());
    }
    
    @Test
    void shouldGetAgenda() {
        when(agendaRepository.findById(agendaId)).thenReturn(Optional.of(mockAgenda));
        
        AgendaResponse response = agendaService.getAgenda(agendaId);
        
        assertNotNull(response);
        assertEquals(agendaId, response.getId());
        verify(agendaRepository).findById(agendaId);
    }
    
    @Test
    void shouldThrowExceptionWhenGettingNonExistentAgenda() {
        when(agendaRepository.findById(agendaId)).thenReturn(Optional.empty());
        
        assertThrows(IllegalArgumentException.class, () -> {
            agendaService.getAgenda(agendaId);
        });
        
        verify(agendaRepository).findById(agendaId);
    }
}