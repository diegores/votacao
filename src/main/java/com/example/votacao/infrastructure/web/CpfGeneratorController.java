package com.example.votacao.infrastructure.web;

import com.example.votacao.infrastructure.util.CpfGenerator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cpf-generator")
@Tag(name = "CPF Generator", description = "Utility APIs for generating valid Brazilian CPFs for testing and development purposes")
public class CpfGeneratorController {
    
    @GetMapping("/generate")
    @Operation(
        summary = "Generate a valid CPF", 
        description = """
        Generates a random valid Brazilian CPF for testing purposes.
        
        **Features:**
        - Mathematically valid CPF generation
        - Returns both raw and formatted versions
        - Useful for testing member registration and voting features
        
        **Note:** Generated CPFs are for testing only and do not represent real people.
        """
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Valid CPF generated successfully",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = """
                    {
                      "cpf": "12345678901",
                      "formatted": "123.456.789-01"
                    }
                    """
                )
            )
        )
    })
    public ResponseEntity<Map<String, String>> generateCpf() {
        String cpf = CpfGenerator.generateValidCpf();
        String formattedCpf = CpfGenerator.generateFormattedValidCpf();
        
        return ResponseEntity.ok(Map.of(
                "cpf", cpf,
                "formatted", formattedCpf
        ));
    }
    
    @GetMapping("/v1/samples")
    @Operation(
        summary = "Get sample valid CPFs", 
        description = """
        Returns a list of pre-generated valid Brazilian CPFs for testing and development.
        
        **Use Cases:**
        - Bulk testing of member registration
        - Development environment setup
        - API testing and validation
        
        **Note:** All CPFs are mathematically valid but for testing purposes only.
        """
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Sample CPFs retrieved successfully",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = """
                    {
                      "cpfs": [
                        "12345678901",
                        "98765432109",
                        "45678912345",
                        "78912345678",
                        "32165498712"
                      ]
                    }
                    """
                )
            )
        )
    })
    public ResponseEntity<Map<String, List<String>>> getSampleCpfs() {
        List<String> samples = Arrays.asList(CpfGenerator.getSampleValidCpfs());
        
        return ResponseEntity.ok(Map.of("cpfs", samples));
    }
}