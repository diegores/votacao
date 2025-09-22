package com.example.votacao.infrastructure.web;

import com.example.votacao.application.dto.CreateVoteRequest;
import com.example.votacao.application.dto.VoteResponse;
import com.example.votacao.application.usecase.VotingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174"})
@RestController
@RequestMapping("/api/agendas")
@RequiredArgsConstructor
@Tag(name = "Voting", description = "Individual vote submission operations. Members can vote 'SIM' (Yes) or 'NAO' (No) on open agenda sessions.")
public class VotingController {
    
    private final VotingService votingService;
    
    @PostMapping("/v1/{agendaId}/votes")
    @Operation(
        summary = "Submit a vote", 
        description = "Submit a vote for a specific agenda. Each member can vote only once per agenda. The agenda must have an open voting session."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Vote submitted successfully"
        ),
        @ApiResponse(
            responseCode = "400", 
            description = "Invalid request data or business rule violation",
            content = @Content(
                mediaType = "application/json",
                examples = {
                    @ExampleObject(
                        name = "Member already voted",
                        value = """
                        {
                          "code": "MEMBER_ALREADY_VOTED",
                          "message": "Membro votou em outra agenda",
                          "timestamp": "2025-09-19T10:30:00"
                        }
                        """
                    ),
                    @ExampleObject(
                        name = "Voting session closed",
                        value = """
                        {
                          "code": "VOTING_SESSION_CLOSED",
                          "message": "Voting session is not open for this agenda",
                          "timestamp": "2025-09-19T10:30:00"
                        }
                        """
                    )
                }
            )
        ),
        @ApiResponse(
            responseCode = "404", 
            description = "Agenda or member not found",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = """
                    {
                      "code": "AGENDA_NOT_FOUND",
                      "message": "Agenda not found with ID: 123e4567-e89b-12d3-a456-426614174000",
                      "timestamp": "2025-09-19T10:30:00"
                    }
                    """
                )
            )
        )
    })
    public ResponseEntity<Void> vote(
            @Parameter(
                description = "Agenda ID to vote on", 
                example = "123e4567-e89b-12d3-a456-426614174000"
            ) 
            @PathVariable UUID agendaId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Vote submission details",
                required = true,
                content = @Content(
                    examples = {
                        @ExampleObject(
                            name = "Vote Yes",
                            value = """
                            {
                              "memberCpf": "12345678901",
                              "voteValue": "SIM"
                            }
                            """
                        ),
                        @ExampleObject(
                            name = "Vote No",
                            value = """
                            {
                              "memberCpf": "98765432109",
                              "voteValue": "NAO"
                            }
                            """
                        )
                    }
                )
            )
            @Valid @RequestBody CreateVoteRequest request) {
        votingService.vote(agendaId, request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/v1/{agendaId}/votes")
    @Operation(
        summary = "Get votes for an agenda",
        description = "Retrieve all votes submitted for a specific agenda."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Votes retrieved successfully"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Agenda not found",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = """
                    {
                      "code": "AGENDA_NOT_FOUND",
                      "message": "Agenda not found with ID: 123e4567-e89b-12d3-a456-426614174000",
                      "timestamp": "2025-09-19T10:30:00"
                    }
                    """
                )
            )
        )
    })
    public ResponseEntity<List<VoteResponse>> getVotes(
            @Parameter(
                description = "Agenda ID to get votes for",
                example = "123e4567-e89b-12d3-a456-426614174000"
            )
            @PathVariable UUID agendaId) {
        List<VoteResponse> votes = votingService.getVotesByAgenda(agendaId);
        return ResponseEntity.ok(votes);
    }
}