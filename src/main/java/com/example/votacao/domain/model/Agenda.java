package com.example.votacao.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Domain entity representing a voting agenda in the cooperative system.
 * 
 * An agenda contains the topic to be voted on and manages the voting session
 * lifecycle. It enforces business rules such as:
 * - Single vote per member per agenda
 * - Time-bounded voting sessions
 * - Vote counting and result calculation
 * 
 * Design Decision: Rich domain model that encapsulates business logic
 * rather than having anemic data structures with external services.
 */
@Entity
@Table(name = "agendas", indexes = {
    @Index(name = "idx_agenda_status", columnList = "status"),
    @Index(name = "idx_agenda_session_times", columnList = "sessionStartTime, sessionEndTime"),
    @Index(name = "idx_agenda_created_at", columnList = "createdAt")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Agenda {
    
    @Id
    private UUID id;
    
    @Column(nullable = false)
    private String title;
    
    @Column(length = 1000)
    private String description;
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VotingSessionStatus status;
    
    private LocalDateTime sessionStartTime;
    
    private LocalDateTime sessionEndTime;
    
    @OneToMany(mappedBy = "agenda", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Vote> votes = new ArrayList<>();
    
    public Agenda(String title, String description) {
        this.id = UUID.randomUUID();
        this.title = title;
        this.description = description;
        this.createdAt = LocalDateTime.now();
        this.status = VotingSessionStatus.CLOSED;
    }
    
    public void openVotingSession(int durationInMinutes) {
        if (this.status == VotingSessionStatus.OPEN) {
            throw new IllegalStateException("Voting session is already open for this agenda");
        }
        
        this.status = VotingSessionStatus.OPEN;
        this.sessionStartTime = LocalDateTime.now();
        this.sessionEndTime = this.sessionStartTime.plusMinutes(durationInMinutes);
    }
    
    public void closeVotingSession() {
        this.status = VotingSessionStatus.CLOSED;
    }
    
    public boolean isVotingOpen() {
        return status == VotingSessionStatus.OPEN && 
               LocalDateTime.now().isBefore(sessionEndTime);
    }
    
    public boolean hasVoted(UUID memberId) {
        return votes.stream()
                .anyMatch(vote -> vote.getMemberId().equals(memberId));
    }
    
    public void addVote(Vote vote) {
        if (!isVotingOpen()) {
            throw new IllegalStateException("Voting session is not open");
        }
        
        if (hasVoted(vote.getMemberId())) {
            throw new IllegalStateException("Member has already voted on this agenda");
        }
        
        this.votes.add(vote);
    }
    
    public VotingResult getVotingResult() {
        long yesCount = votes.stream()
                .filter(vote -> vote.getVoteType() == VoteType.YES)
                .count();
        
        long noCount = votes.stream()
                .filter(vote -> vote.getVoteType() == VoteType.NO)
                .count();
        
        return new VotingResult(yesCount, noCount, votes.size());
    }
}