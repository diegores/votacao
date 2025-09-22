package com.example.votacao.application.dto;

import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OpenVotingSessionRequest {
    
    @Min(value = 1, message = "Duration must be at least 1 minute")
    private Integer durationInMinutes = 1;
}