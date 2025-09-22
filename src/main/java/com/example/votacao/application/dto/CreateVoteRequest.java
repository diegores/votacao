package com.example.votacao.application.dto;

import com.example.votacao.domain.model.VoteType;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateVoteRequest {

    private UUID memberId;

    private String memberCpf;

    private VoteType voteType;

    // Support legacy voteValue for SIM/NAO
    private String voteValue;

    // Convenience constructor for backward compatibility
    public CreateVoteRequest(UUID memberId, VoteType voteType) {
        this.memberId = memberId;
        this.voteType = voteType;
    }

    // Custom setter to handle both UUID and CPF strings in memberId field
    @JsonSetter("memberId")
    public void setMemberIdFromJson(String value) {
        if (value == null || value.trim().isEmpty()) {
            this.memberId = null;
            return;
        }

        // Try to parse as UUID first
        try {
            this.memberId = UUID.fromString(value);
        } catch (IllegalArgumentException e) {
            // If it's not a valid UUID, treat it as a CPF and store in memberCpf
            this.memberCpf = value;
            this.memberId = null;
        }
    }

    public VoteType getVoteType() {
        if (voteType != null) {
            return voteType;
        }
        if (voteValue != null) {
            return switch (voteValue.toUpperCase()) {
                case "SIM", "YES" -> VoteType.YES;
                case "NAO", "NO" -> VoteType.NO;
                default -> throw new IllegalArgumentException("Invalid vote value: " + voteValue);
            };
        }
        throw new IllegalArgumentException("Either voteType or voteValue must be provided");
    }
}