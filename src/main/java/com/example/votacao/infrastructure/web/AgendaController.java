package com.example.votacao.infrastructure.web;

import com.example.votacao.application.dto.*;
import com.example.votacao.application.usecase.AgendaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174"})
@RestController
@RequestMapping("/api/agendas")
@RequiredArgsConstructor
@Tag(name = "Agendas", description = "Voting agenda management operations. Agendas are the topics that members vote on during cooperative sessions.")
public class AgendaController {
    
    private final AgendaService agendaService;
    
    @PostMapping("/v1")
    @Operation(
        summary = "Create a new agenda", 
        description = "Creates a new voting agenda. The agenda will be in CLOSED status initially and requires opening a voting session to accept votes."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201", 
            description = "Agenda created successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = AgendaResponse.class),
                examples = @ExampleObject(
                    name = "Created Agenda",
                    value = """
                    {
                      "id": "123e4567-e89b-12d3-a456-426614174000",
                      "title": "Budget Approval 2024",
                      "description": "Vote to approve the annual budget for 2024",
                      "createdAt": "2025-09-19T10:30:00",
                      "status": "CLOSED",
                      "sessionStartTime": null,
                      "sessionEndTime": null,
                      "votingOpen": false
                    }
                    """
                )
            )
        ),
        @ApiResponse(
            responseCode = "400", 
            description = "Invalid request data",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = """
                    {
                      "code": "VALIDATION_ERROR",
                      "message": "Invalid request data",
                      "timestamp": "2025-09-19T10:30:00",
                      "details": {
                        "title": "Title is required"
                      }
                    }
                    """
                )
            )
        )
    })
    public ResponseEntity<AgendaResponse> createAgenda(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Agenda creation details",
                required = true,
                content = @Content(
                    examples = @ExampleObject(
                        name = "Create Agenda Example",
                        value = """
                        {
                          "title": "Budget Approval 2024",
                          "description": "Vote to approve the annual budget for 2024"
                        }
                        """
                    )
                )
            )
            @Valid @RequestBody CreateAgendaRequest request) {
        AgendaResponse response = agendaService.createAgenda(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @PostMapping("/v1/{agendaId}/voting-session")
    @Operation(summary = "Open voting session", description = "Open a voting session for a specific agenda")
    public ResponseEntity<AgendaResponse> openVotingSession(
            @Parameter(description = "Agenda ID") @PathVariable UUID agendaId,
            @Valid @RequestBody OpenVotingSessionRequest request) {
        AgendaResponse response = agendaService.openVotingSession(agendaId, request);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/v1/{agendaId}")
    @Operation(summary = "Get agenda details", description = "Retrieve details of a specific agenda")
    public ResponseEntity<AgendaResponse> getAgenda(
            @Parameter(description = "Agenda ID") @PathVariable UUID agendaId) {
        AgendaResponse response = agendaService.getAgenda(agendaId);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/v1")
    @Operation(summary = "Get all agendas", description = "Retrieve all agendas")
    public ResponseEntity<List<AgendaResponse>> getAllAgendas() {
        List<AgendaResponse> response = agendaService.getAllAgendas();
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/v1/voting-sessions/open")
    @Operation(summary = "Get open voting sessions", description = "Retrieve all agendas with open voting sessions")
    public ResponseEntity<List<AgendaResponse>> getOpenVotingSessions() {
        List<AgendaResponse> response = agendaService.getOpenVotingSessions();
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/v1/{agendaId}/result")
    @Operation(summary = "Get voting result", description = "Get the voting result for a specific agenda")
    public ResponseEntity<VotingResultResponse> getVotingResult(
            @Parameter(description = "Agenda ID") @PathVariable UUID agendaId) {
        VotingResultResponse response = agendaService.getVotingResult(agendaId);
        return ResponseEntity.ok(response);
    }
}