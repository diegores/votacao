package com.example.votacao.infrastructure.config;

import com.example.votacao.application.usecase.AgendaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class VotingSessionScheduler {
    
    private final AgendaService agendaService;
    
    @Scheduled(fixedRate = 60000) // Run every minute
    public void closeExpiredVotingSessions() {
        log.debug("Checking for expired voting sessions...");
        try {
            agendaService.closeExpiredVotingSessions();
        } catch (Exception e) {
            log.error("Error closing expired voting sessions", e);
        }
    }
}