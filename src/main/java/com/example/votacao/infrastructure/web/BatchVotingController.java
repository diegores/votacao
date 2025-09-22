package com.example.votacao.infrastructure.web;

import com.example.votacao.application.dto.BatchVotingRequest;
import com.example.votacao.application.dto.BatchVotingResponse;
import com.example.votacao.application.usecase.BatchVotingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/batch-voting")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Batch Voting", description = "High-performance batch voting operations")
public class BatchVotingController {
    
    private final BatchVotingService batchVotingService;
    
    @PostMapping("/v1/votes")
    @Operation(
        summary = "Submit multiple votes in batch", 
        description = """
        Processes multiple votes for an agenda in a single optimized operation for high-performance scenarios.
        
        **Performance Features:**
        - Maximum batch size: 10,000 votes per request
        - Optimized database operations with batch processing
        - Partial success handling - continues processing even if some votes fail
        - Detailed response statistics including success/failure counts
        
        **Business Rules:**
        - Each member can vote only once per agenda
        - Agenda must have an open voting session
        - Invalid votes are logged but don't stop processing
        - Duplicate votes for same member are automatically filtered
        """
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "All votes processed successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = BatchVotingResponse.class),
                examples = @ExampleObject(
                    name = "Successful batch processing",
                    value = """
                    {
                      "agendaId": "123e4567-e89b-12d3-a456-426614174000",
                      "totalVotes": 1000,
                      "successfulVotes": 1000,
                      "failedVotes": 0,
                      "processingTimeMs": 342,
                      "errors": []
                    }
                    """
                )
            )
        ),
        @ApiResponse(
            responseCode = "206", 
            description = "Partial success - some votes failed",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = BatchVotingResponse.class),
                examples = @ExampleObject(
                    name = "Partial success response",
                    value = """
                    {
                      "agendaId": "123e4567-e89b-12d3-a456-426614174000",
                      "totalVotes": 1000,
                      "successfulVotes": 985,
                      "failedVotes": 15,
                      "processingTimeMs": 456,
                      "errors": [
                        "Member 12345678901 has already voted",
                        "Invalid CPF: 11111111111"
                      ]
                    }
                    """
                )
            )
        ),
        @ApiResponse(
            responseCode = "400", 
            description = "Invalid request or voting session closed",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = """
                    {
                      "code": "VOTING_SESSION_CLOSED",
                      "message": "Voting session is not open for this agenda",
                      "timestamp": "2025-09-19T10:30:00"
                    }
                    """
                )
            )
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
    public ResponseEntity<BatchVotingResponse> processBatchVotes(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Batch voting request with agenda ID and list of votes",
                required = true,
                content = @Content(
                    examples = @ExampleObject(
                        name = "Batch voting example",
                        value = """
                        {
                          "agendaId": "123e4567-e89b-12d3-a456-426614174000",
                          "votes": [
                            {
                              "memberCpf": "12345678901",
                              "voteValue": "SIM"
                            },
                            {
                              "memberCpf": "98765432109",
                              "voteValue": "NAO"
                            },
                            {
                              "memberCpf": "45678912345",
                              "voteValue": "SIM"
                            }
                          ]
                        }
                        """
                    )
                )
            )
            @Valid @RequestBody BatchVotingRequest request) {
        
        log.info("Received batch voting request for agenda {} with {} votes", 
                request.getAgendaId(), request.getVotes().size());
        
        try {
            BatchVotingResponse response = batchVotingService.processBatchVotes(request);
            
            if (response.getFailedVotes() > 0) {
                return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT).body(response);
            }
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            log.error("Invalid batch voting request: {}", e.getMessage());
            return ResponseEntity.notFound().build();
            
        } catch (IllegalStateException e) {
            log.error("Invalid voting state: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
            
        } catch (Exception e) {
            log.error("Error processing batch votes", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}