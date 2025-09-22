package com.example.votacao.application.usecase;

import com.example.votacao.application.dto.BatchVotingRequest;
import com.example.votacao.application.dto.BatchVotingResponse;
import com.example.votacao.domain.model.Agenda;
import com.example.votacao.domain.model.Vote;
import com.example.votacao.domain.repository.AgendaRepository;
import com.example.votacao.domain.repository.VoteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Service for processing votes in batches for improved performance.
 * 
 * This service provides a simplified approach to batch voting that:
 * - Validates all votes before processing
 * - Prevents duplicate votes 
 * - Maintains transactional consistency
 * - Provides clear error reporting
 * 
 * Design Decision: Kept simple without complex chunking or parallel processing
 * to ensure maintainability and reduce complexity.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BatchVotingService {
    
    private final AgendaRepository agendaRepository;
    private final VoteRepository voteRepository;
    
    /**
     * Processes a batch of votes for a specific agenda.
     * 
     * @param request the batch voting request containing agenda ID and votes
     * @return response with processing results and statistics
     * @throws IllegalArgumentException if agenda not found
     * @throws IllegalStateException if voting session is not open
     */
    @Transactional
    @CacheEvict(value = {"voting-results", "agendas"}, key = "#request.agendaId")
    public BatchVotingResponse processBatchVotes(BatchVotingRequest request) {
        long startTime = System.currentTimeMillis();
        
        log.info("Processing batch of {} votes for agenda {}", 
                request.getVotes().size(), request.getAgendaId());
        
        // 1. Validate agenda and voting session
        Agenda agenda = findAndValidateAgenda(request.getAgendaId());
        
        // 2. Find existing votes to prevent duplicates
        Set<UUID> existingVoterIds = getExistingVoterIds(agenda.getId());
        
        // 3. Process votes and separate valid from invalid
        List<Vote> validVotes = new ArrayList<>();
        List<UUID> failedMemberIds = new ArrayList<>();
        
        for (var voteRequest : request.getVotes()) {
            if (existingVoterIds.contains(voteRequest.getMemberId())) {
                log.debug("Member {} already voted on agenda {}", 
                         voteRequest.getMemberId(), agenda.getId());
                failedMemberIds.add(voteRequest.getMemberId());
            } else {
                Vote vote = new Vote(agenda, voteRequest.getMemberId(), voteRequest.getVoteType());
                validVotes.add(vote);
                existingVoterIds.add(voteRequest.getMemberId()); // Prevent duplicates within batch
            }
        }
        
        // 4. Save all valid votes in a single transaction
        if (!validVotes.isEmpty()) {
            voteRepository.saveAll(validVotes);
            log.info("Successfully saved {} votes", validVotes.size());
        }
        
        long processingTime = System.currentTimeMillis() - startTime;
        
        log.info("Batch processing completed: {} successful, {} failed, {}ms", 
                validVotes.size(), failedMemberIds.size(), processingTime);
        
        // 5. Return appropriate response
        if (failedMemberIds.isEmpty()) {
            return BatchVotingResponse.success(validVotes.size(), processingTime);
        } else {
            return BatchVotingResponse.partial(validVotes.size(), failedMemberIds.size(), 
                                             failedMemberIds, processingTime);
        }
    }
    
    /**
     * Finds and validates the agenda for voting.
     */
    private Agenda findAndValidateAgenda(UUID agendaId) {
        Agenda agenda = agendaRepository.findById(agendaId)
                .orElseThrow(() -> new IllegalArgumentException("Agenda not found: " + agendaId));
        
        if (!agenda.isVotingOpen()) {
            throw new IllegalStateException("Voting session is not open for agenda: " + agendaId);
        }
        
        return agenda;
    }
    
    /**
     * Gets all member IDs who have already voted on this agenda.
     */
    private Set<UUID> getExistingVoterIds(UUID agendaId) {
        List<Vote> existingVotes = voteRepository.findByAgendaId(agendaId);
        Set<UUID> voterIds = new HashSet<>();
        for (Vote vote : existingVotes) {
            voterIds.add(vote.getMemberId());
        }
        return voterIds;
    }
}