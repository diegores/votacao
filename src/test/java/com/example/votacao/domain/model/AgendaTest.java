package com.example.votacao.domain.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.UUID;

class AgendaTest {
    
    private Agenda agenda;
    
    @BeforeEach
    void setUp() {
        agenda = new Agenda("Test Agenda", "Test Description");
    }
    
    @Test
    void shouldCreateAgendaWithCorrectInitialState() {
        assertNotNull(agenda.getId());
        assertEquals("Test Agenda", agenda.getTitle());
        assertEquals("Test Description", agenda.getDescription());
        assertEquals(VotingSessionStatus.CLOSED, agenda.getStatus());
        assertNotNull(agenda.getCreatedAt());
        assertNull(agenda.getSessionStartTime());
        assertNull(agenda.getSessionEndTime());
        assertFalse(agenda.isVotingOpen());
    }
    
    @Test
    void shouldOpenVotingSession() {
        agenda.openVotingSession(5);
        
        assertEquals(VotingSessionStatus.OPEN, agenda.getStatus());
        assertNotNull(agenda.getSessionStartTime());
        assertNotNull(agenda.getSessionEndTime());
        assertTrue(agenda.isVotingOpen());
        assertTrue(agenda.getSessionEndTime().isAfter(agenda.getSessionStartTime()));
    }
    
    @Test
    void shouldNotOpenVotingSessionWhenAlreadyOpen() {
        agenda.openVotingSession(5);
        
        assertThrows(IllegalStateException.class, () -> {
            agenda.openVotingSession(3);
        });
    }
    
    @Test
    void shouldCloseVotingSession() {
        agenda.openVotingSession(5);
        agenda.closeVotingSession();
        
        assertEquals(VotingSessionStatus.CLOSED, agenda.getStatus());
    }
    
    @Test
    void shouldDetectIfMemberHasVoted() {
        UUID memberId = UUID.randomUUID();
        agenda.openVotingSession(5);
        
        assertFalse(agenda.hasVoted(memberId));
        
        Vote vote = new Vote(agenda, memberId, VoteType.YES);
        agenda.getVotes().add(vote);
        
        assertTrue(agenda.hasVoted(memberId));
    }
    
    @Test
    void shouldAddVoteWhenVotingIsOpen() {
        UUID memberId = UUID.randomUUID();
        agenda.openVotingSession(5);
        
        Vote vote = new Vote(agenda, memberId, VoteType.YES);
        agenda.addVote(vote);
        
        assertEquals(1, agenda.getVotes().size());
        assertTrue(agenda.hasVoted(memberId));
    }
    
    @Test
    void shouldNotAddVoteWhenVotingIsClosed() {
        UUID memberId = UUID.randomUUID();
        Vote vote = new Vote(agenda, memberId, VoteType.YES);
        
        assertThrows(IllegalStateException.class, () -> {
            agenda.addVote(vote);
        });
    }
    
    @Test
    void shouldNotAddVoteWhenMemberAlreadyVoted() {
        UUID memberId = UUID.randomUUID();
        agenda.openVotingSession(5);
        
        Vote vote1 = new Vote(agenda, memberId, VoteType.YES);
        agenda.addVote(vote1);
        
        Vote vote2 = new Vote(agenda, memberId, VoteType.NO);
        assertThrows(IllegalStateException.class, () -> {
            agenda.addVote(vote2);
        });
    }
    
    @Test
    void shouldCalculateVotingResult() {
        agenda.openVotingSession(5);
        
        UUID member1 = UUID.randomUUID();
        UUID member2 = UUID.randomUUID();
        UUID member3 = UUID.randomUUID();
        
        agenda.addVote(new Vote(agenda, member1, VoteType.YES));
        agenda.addVote(new Vote(agenda, member2, VoteType.YES));
        agenda.addVote(new Vote(agenda, member3, VoteType.NO));
        
        VotingResult result = agenda.getVotingResult();
        
        assertEquals(2, result.getYesVotes());
        assertEquals(1, result.getNoVotes());
        assertEquals(3, result.getTotalVotes());
        assertEquals("APPROVED", result.getResult());
    }
}