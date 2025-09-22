package com.example.votacao.application.usecase;

import com.example.votacao.application.dto.BatchVoteRequest;
import com.example.votacao.application.dto.BatchVotingRequest;
import com.example.votacao.application.dto.BatchVotingResponse;
import com.example.votacao.domain.model.Agenda;
import com.example.votacao.domain.model.Vote;
import com.example.votacao.domain.model.VoteType;
import com.example.votacao.domain.model.VotingSessionStatus;
import com.example.votacao.domain.repository.AgendaRepository;
import com.example.votacao.domain.repository.VoteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

/**
 * Unit tests for BatchVotingService.
 * 
 * Tests the core batch voting functionality including:
 * - Successful batch processing
 * - Duplicate vote handling
 * - Validation errors
 * - Edge cases
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("BatchVotingService Tests")
class BatchVotingServiceTest {
    
    @Mock
    private AgendaRepository agendaRepository;
    
    @Mock
    private VoteRepository voteRepository;
    
    @InjectMocks
    private BatchVotingService batchVotingService;
    
    private UUID agendaId;
    private Agenda agenda;
    private BatchVotingRequest batchRequest;
    
    @BeforeEach
    void setUp() {
        agendaId = UUID.randomUUID();
        
        // Create test agenda
        agenda = new Agenda("Test Agenda", "Test Description");
        agenda.setId(agendaId);
        agenda.setStatus(VotingSessionStatus.OPEN);
        agenda.setSessionStartTime(LocalDateTime.now().minusMinutes(5));
        agenda.setSessionEndTime(LocalDateTime.now().plusMinutes(10));
        
        // Create batch request with 3 votes
        List<BatchVoteRequest> votes = Arrays.asList(
            new BatchVoteRequest(UUID.randomUUID(), VoteType.YES),
            new BatchVoteRequest(UUID.randomUUID(), VoteType.NO),
            new BatchVoteRequest(UUID.randomUUID(), VoteType.YES)
        );
        batchRequest = new BatchVotingRequest(agendaId, votes);
    }
    
    @Test
    @DisplayName("Should process all votes successfully when no duplicates exist")
    void shouldProcessAllVotesSuccessfully() {
        // Given
        when(agendaRepository.findById(agendaId)).thenReturn(Optional.of(agenda));
        when(voteRepository.findByAgendaId(agendaId)).thenReturn(List.of()); // No existing votes
        when(voteRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));
        
        // When
        BatchVotingResponse response = batchVotingService.processBatchVotes(batchRequest);
        
        // Then
        assertThat(response.getSuccessfulVotes()).isEqualTo(3);
        assertThat(response.getFailedVotes()).isEqualTo(0);
        assertThat(response.getFailedMemberIds()).isEmpty();
        assertThat(response.getProcessingTimeMs()).isGreaterThanOrEqualTo(0);
        
        verify(voteRepository).saveAll(argThat(votes -> votes.size() == 3));
        verify(agendaRepository).findById(agendaId);
    }
    
    @Test
    @DisplayName("Should handle partial success when some members already voted")
    void shouldHandlePartialSuccessWithDuplicates() {
        // Given
        UUID existingVoterId = batchRequest.getVotes().get(0).getMemberId();
        Vote existingVote = new Vote(agenda, existingVoterId, VoteType.NO);
        
        when(agendaRepository.findById(agendaId)).thenReturn(Optional.of(agenda));
        when(voteRepository.findByAgendaId(agendaId)).thenReturn(List.of(existingVote));
        when(voteRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));
        
        // When
        BatchVotingResponse response = batchVotingService.processBatchVotes(batchRequest);
        
        // Then
        assertThat(response.getSuccessfulVotes()).isEqualTo(2); // 3 - 1 duplicate
        assertThat(response.getFailedVotes()).isEqualTo(1);
        assertThat(response.getFailedMemberIds()).containsExactly(existingVoterId);
        
        verify(voteRepository).saveAll(argThat(votes -> votes.size() == 2));
    }
    
    @Test
    @DisplayName("Should throw exception when agenda not found")
    void shouldThrowExceptionWhenAgendaNotFound() {
        // Given
        when(agendaRepository.findById(agendaId)).thenReturn(Optional.empty());
        
        // When & Then
        assertThatThrownBy(() -> batchVotingService.processBatchVotes(batchRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Agenda not found");
        
        verify(voteRepository, never()).saveAll(any());
    }
    
    @Test
    @DisplayName("Should throw exception when voting session is closed")
    void shouldThrowExceptionWhenVotingSessionClosed() {
        // Given
        agenda.setStatus(VotingSessionStatus.CLOSED);
        when(agendaRepository.findById(agendaId)).thenReturn(Optional.of(agenda));
        
        // When & Then
        assertThatThrownBy(() -> batchVotingService.processBatchVotes(batchRequest))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Voting session is not open");
        
        verify(voteRepository, never()).saveAll(any());
    }
    
    @Test
    @DisplayName("Should handle empty vote list")
    void shouldHandleEmptyVoteList() {
        // Given
        BatchVotingRequest emptyRequest = new BatchVotingRequest(agendaId, List.of());
        when(agendaRepository.findById(agendaId)).thenReturn(Optional.of(agenda));
        when(voteRepository.findByAgendaId(agendaId)).thenReturn(List.of());
        
        // When
        BatchVotingResponse response = batchVotingService.processBatchVotes(emptyRequest);
        
        // Then
        assertThat(response.getSuccessfulVotes()).isEqualTo(0);
        assertThat(response.getFailedVotes()).isEqualTo(0);
        assertThat(response.getFailedMemberIds()).isEmpty();
        
        verify(voteRepository, never()).saveAll(any());
    }
    
    @Test
    @DisplayName("Should prevent duplicate votes within the same batch")
    void shouldPreventDuplicateVotesWithinBatch() {
        // Given
        UUID duplicateMemberId = UUID.randomUUID();
        List<BatchVoteRequest> votesWithDuplicates = Arrays.asList(
            new BatchVoteRequest(duplicateMemberId, VoteType.YES),
            new BatchVoteRequest(UUID.randomUUID(), VoteType.NO),
            new BatchVoteRequest(duplicateMemberId, VoteType.NO) // Duplicate within batch
        );
        BatchVotingRequest requestWithDuplicates = new BatchVotingRequest(agendaId, votesWithDuplicates);
        
        when(agendaRepository.findById(agendaId)).thenReturn(Optional.of(agenda));
        when(voteRepository.findByAgendaId(agendaId)).thenReturn(List.of());
        when(voteRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));
        
        // When
        BatchVotingResponse response = batchVotingService.processBatchVotes(requestWithDuplicates);
        
        // Then
        assertThat(response.getSuccessfulVotes()).isEqualTo(2); // Only first occurrence of each member
        assertThat(response.getFailedVotes()).isEqualTo(1);
        assertThat(response.getFailedMemberIds()).containsExactly(duplicateMemberId);
        
        verify(voteRepository).saveAll(argThat(votes -> votes.size() == 2));
    }
    
    @Test
    @DisplayName("Should handle voting session time expiration")
    void shouldHandleVotingSessionTimeExpiration() {
        // Given
        agenda.setSessionEndTime(LocalDateTime.now().minusMinutes(1)); // Expired
        when(agendaRepository.findById(agendaId)).thenReturn(Optional.of(agenda));
        
        // When & Then
        assertThatThrownBy(() -> batchVotingService.processBatchVotes(batchRequest))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Voting session is not open");
    }
}