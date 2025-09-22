package com.example.votacao.infrastructure.web;

import com.example.votacao.domain.model.CpfValidationResponse;
import com.example.votacao.domain.service.CpfValidationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cpf")
@RequiredArgsConstructor
@Tag(name = "CPF Validation", description = "Brazilian CPF validation and eligibility checking. Validates CPF format and checks voting eligibility status.")
public class CpfValidationController {
    
    private final CpfValidationService cpfValidationService;
    
    @GetMapping("/v1/validate/{cpf}")
    @Operation(
        summary = "Validate CPF and check voting eligibility", 
        description = """
        Validates a Brazilian CPF (Cadastro de Pessoas Físicas) and checks if the person is eligible to vote.
        
        **Validation Features:**
        - CPF format validation (11 digits)
        - Mathematical checksum verification
        - Voting eligibility status check
        - Integration with external CPF validation services
        
        **Business Rules:**
        - CPF must be exactly 11 digits
        - Cannot be a sequence of repeated digits (e.g., 11111111111)
        - Must pass Brazilian CPF algorithm validation
        - Person must be eligible to vote (not deceased, not suspended, etc.)
        """
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "CPF is valid and person is eligible to vote",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = CpfValidationResponse.class),
                examples = @ExampleObject(
                    name = "Valid CPF response",
                    value = """
                    {
                      "cpf": "12345678901",
                      "valid": true,
                      "ableToVote": true,
                      "message": "CPF válido e apto para votação"
                    }
                    """
                )
            )
        ),
        @ApiResponse(
            responseCode = "404", 
            description = "CPF is invalid or person is not eligible to vote",
            content = @Content(
                mediaType = "application/json",
                examples = {
                    @ExampleObject(
                        name = "Invalid CPF format",
                        value = """
                        {
                          "code": "INVALID_CPF",
                          "message": "CPF format is invalid",
                          "timestamp": "2025-09-19T10:30:00"
                        }
                        """
                    ),
                    @ExampleObject(
                        name = "Not eligible to vote",
                        value = """
                        {
                          "code": "NOT_ELIGIBLE_TO_VOTE",
                          "message": "Person is not eligible to vote",
                          "timestamp": "2025-09-19T10:30:00"
                        }
                        """
                    )
                }
            )
        ),
        @ApiResponse(
            responseCode = "400", 
            description = "Invalid request format",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = """
                    {
                      "code": "INVALID_REQUEST",
                      "message": "CPF must contain exactly 11 digits",
                      "timestamp": "2025-09-19T10:30:00"
                    }
                    """
                )
            )
        ),
        @ApiResponse(
            responseCode = "503", 
            description = "External CPF validation service unavailable",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = """
                    {
                      "code": "SERVICE_UNAVAILABLE",
                      "message": "CPF validation service is temporarily unavailable",
                      "timestamp": "2025-09-19T10:30:00"
                    }
                    """
                )
            )
        )
    })
    public ResponseEntity<CpfValidationResponse> validateCpf(
            @Parameter(
                description = "Brazilian CPF to validate (11 digits, numbers only)", 
                example = "12345678901"
            ) 
            @PathVariable String cpf) {
        CpfValidationResponse response = cpfValidationService.validateCpf(cpf);
        return ResponseEntity.ok(response);
    }
}