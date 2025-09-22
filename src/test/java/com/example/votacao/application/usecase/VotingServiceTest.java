package com.example.votacao.application.usecase;

import com.example.votacao.application.dto.CreateVoteRequest;
import com.example.votacao.domain.model.Agenda;
import com.example.votacao.domain.model.VoteType;
import com.example.votacao.domain.repository.AgendaRepository;
import com.example.votacao.domain.repository.MemberRepository;
import com.example.votacao.domain.repository.VoteRepository;
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
class VotingServiceTest {
    
    @Mock
    private AgendaRepository agendaRepository;
    
    @Mock
    private MemberRepository memberRepository;
    
    @Mock
    private VoteRepository voteRepository;
    
    @InjectMocks
    private VotingService votingService;
    
    private UUID agendaId;
    private UUID memberId;
    private Agenda mockAgenda;
    
    @BeforeEach
    void setUp() {
        agendaId = UUID.randomUUID();
        memberId = UUID.randomUUID();
        mockAgenda = new Agenda("Test Agenda", "Description");
        mockAgenda.setId(agendaId);
        mockAgenda.openVotingSession(5);
    }
    
    @Test
    void shouldVoteSuccessfully() {
        CreateVoteRequest request = new CreateVoteRequest(memberId, VoteType.YES);
        
        when(memberRepository.existsById(memberId)).thenReturn(true);
        when(agendaRepository.findById(agendaId)).thenReturn(Optional.of(mockAgenda));
        when(voteRepository.existsByAgendaIdAndMemberId(agendaId, memberId)).thenReturn(false);
        
        assertDoesNotThrow(() -> {
            votingService.vote(agendaId, request);
        });
        
        verify(memberRepository).existsById(memberId);
        verify(agendaRepository).findById(agendaId);
        verify(voteRepository).existsByAgendaIdAndMemberId(agendaId, memberId);
        verify(voteRepository).save(any());
    }
    
    @Test
    void shouldThrowExceptionWhenMemberDoesNotExist() {
        CreateVoteRequest request = new CreateVoteRequest(memberId, VoteType.YES);
        
        when(memberRepository.existsById(memberId)).thenReturn(false);
        
        assertThrows(IllegalArgumentException.class, () -> {
            votingService.vote(agendaId, request);
        });
        
        verify(memberRepository).existsById(memberId);
        verify(agendaRepository, never()).findById(any());
        verify(voteRepository, never()).save(any());
    }
    
    @Test
    void shouldThrowExceptionWhenAgendaDoesNotExist() {
        CreateVoteRequest request = new CreateVoteRequest(memberId, VoteType.YES);
        
        when(memberRepository.existsById(memberId)).thenReturn(true);
        when(agendaRepository.findById(agendaId)).thenReturn(Optional.empty());
        
        assertThrows(IllegalArgumentException.class, () -> {
            votingService.vote(agendaId, request);
        });
        
        verify(memberRepository).existsById(memberId);
        verify(agendaRepository).findById(agendaId);
        verify(voteRepository, never()).save(any());
    }
    
    @Test
    void shouldThrowExceptionWhenMemberAlreadyVoted() {
        CreateVoteRequest request = new CreateVoteRequest(memberId, VoteType.YES);
        
        when(memberRepository.existsById(memberId)).thenReturn(true);
        when(agendaRepository.findById(agendaId)).thenReturn(Optional.of(mockAgenda));
        when(voteRepository.existsByAgendaIdAndMemberId(agendaId, memberId)).thenReturn(true);
        
        assertThrows(IllegalStateException.class, () -> {
            votingService.vote(agendaId, request);
        });
        
        verify(memberRepository).existsById(memberId);
        verify(agendaRepository).findById(agendaId);
        verify(voteRepository).existsByAgendaIdAndMemberId(agendaId, memberId);
        verify(voteRepository, never()).save(any());
    }
}