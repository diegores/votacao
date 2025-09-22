package com.example.votacao.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "votes", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"agenda_id", "member_id"}),
       indexes = {
           @Index(name = "idx_vote_agenda_id", columnList = "agenda_id"),
           @Index(name = "idx_vote_member_id", columnList = "member_id"),
           @Index(name = "idx_vote_type", columnList = "voteType"),
           @Index(name = "idx_vote_voted_at", columnList = "votedAt"),
           @Index(name = "idx_vote_agenda_member", columnList = "agenda_id, member_id")
       })
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Vote {
    
    @Id
    private UUID id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agenda_id", nullable = false)
    private Agenda agenda;
    
    @Column(name = "member_id", nullable = false)
    private UUID memberId;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VoteType voteType;
    
    @Column(nullable = false)
    private LocalDateTime votedAt;
    
    public Vote(Agenda agenda, UUID memberId, VoteType voteType) {
        this.id = UUID.randomUUID();
        this.agenda = agenda;
        this.memberId = memberId;
        this.voteType = voteType;
        this.votedAt = LocalDateTime.now();
    }
}