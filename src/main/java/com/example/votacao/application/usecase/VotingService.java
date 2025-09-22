package com.example.votacao.application.usecase;

import com.example.votacao.application.dto.CreateVoteRequest;
import com.example.votacao.application.dto.VoteResponse;
import com.example.votacao.domain.model.Agenda;
import com.example.votacao.domain.model.Vote;
import com.example.votacao.domain.repository.AgendaRepository;
import com.example.votacao.domain.repository.MemberRepository;
import com.example.votacao.domain.repository.VoteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Service responsible for individual vote operations.
 * 
 * Handles the core voting business logic including:
 * - Vote validation and authorization
 * - Duplicate vote prevention
 * - Vote persistence
 * 
 * Design Decision: Kept separate from batch operations for single responsibility
 * and to maintain clear separation of concerns.
 */
@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class VotingService {
    
    private final AgendaRepository agendaRepository;
    private final MemberRepository memberRepository;
    private final VoteRepository voteRepository;
    
    /**
     * Submits a vote for a member on a specific agenda.
     * 
     * @param agendaId the agenda to vote on
     * @param request the vote request containing member ID and vote type
     * @throws IllegalArgumentException if member or agenda not found
     * @throws IllegalStateException if voting session is closed or member already voted
     */
    @CacheEvict(value = {"voting-results", "agendas"}, key = "#agendaId")
    public void vote(UUID agendaId, CreateVoteRequest request) {
        // Determine member ID from request
        UUID memberId = resolveMemberId(request);
        log.debug("Processing vote for member {} on agenda {}", memberId, agendaId);

        // Validate member exists
        if (!memberRepository.existsById(memberId)) {
            throw new IllegalArgumentException("Member not found with id: " + memberId);
        }
        
        // Get agenda
        Agenda agenda = agendaRepository.findById(agendaId)
                .orElseThrow(() -> new IllegalArgumentException("Agenda not found with id: " + agendaId));
        
        // Check if voting session is open
        if (!agenda.isVotingOpen()) {
            throw new IllegalStateException("Voting session is not open for this agenda");
        }
        
        // Check if member has already voted
        if (voteRepository.existsByAgendaIdAndMemberId(agendaId, memberId)) {
            throw new IllegalStateException("Member has already voted on this agenda");
        }

        // Create and save vote
        Vote vote = new Vote(agenda, memberId, request.getVoteType());
        voteRepository.save(vote);

        log.info("Vote successfully submitted for member {} on agenda {}", memberId, agendaId);
    }

    /**
     * Retrieves all votes for a specific agenda.
     *
     * @param agendaId the agenda to get votes for
     * @return list of votes for the agenda
     * @throws IllegalArgumentException if agenda not found
     */
    @Cacheable(value = "votes", key = "#agendaId")
    @Transactional(readOnly = true)
    public List<VoteResponse> getVotesByAgenda(UUID agendaId) {
        log.debug("Retrieving votes for agenda {}", agendaId);

        // Validate agenda exists
        if (!agendaRepository.existsById(agendaId)) {
            throw new IllegalArgumentException("Agenda not found with id: " + agendaId);
        }

        List<Vote> votes = voteRepository.findByAgendaId(agendaId);
        return votes.stream()
                .map(vote -> new VoteResponse(
                        vote.getId(),
                        vote.getMemberId(),
                        vote.getVoteType(),
                        vote.getVotedAt()))
                .toList();
    }

    /**
     * Resolves member ID from the request, supporting both direct ID and CPF lookup.
     *
     * @param request the vote request
     * @return the resolved member ID
     * @throws IllegalArgumentException if member cannot be found or identified
     */
    private UUID resolveMemberId(CreateVoteRequest request) {
        if (request.getMemberId() != null) {
            return request.getMemberId();
        }

        if (request.getMemberCpf() != null && !request.getMemberCpf().trim().isEmpty()) {
            return memberRepository.findByCpf(request.getMemberCpf())
                    .map(member -> member.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Member not found with CPF: " + request.getMemberCpf()));
        }

        throw new IllegalArgumentException("Either memberId or memberCpf must be provided");
    }
}