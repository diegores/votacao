package com.example.votacao.infrastructure.web;

import com.example.votacao.application.dto.CreateMemberRequest;
import com.example.votacao.application.usecase.MemberService;
import com.example.votacao.domain.model.Member;
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
@RequestMapping("/api/members")
@RequiredArgsConstructor
@Tag(name = "Members", description = "Cooperative member management operations. Members are registered with CPF validation and eligibility checking.")
public class MemberController {
    
    private final MemberService memberService;
    
    @PostMapping("/v1")
    @Operation(
        summary = "Create a new member", 
        description = "Register a new cooperative member with CPF validation and eligibility checking."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201", 
            description = "Member created successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Member.class),
                examples = @ExampleObject(
                    name = "Created member",
                    value = """
                    {
                      "id": "123e4567-e89b-12d3-a456-426614174000",
                      "cpf": "12345678901",
                      "name": "João Silva",
                      "createdAt": "2025-09-19T10:30:00"
                    }
                    """
                )
            )
        ),
        @ApiResponse(
            responseCode = "400", 
            description = "Invalid request data or CPF already exists",
            content = @Content(
                mediaType = "application/json",
                examples = {
                    @ExampleObject(
                        name = "Invalid CPF",
                        value = """
                        {
                          "code": "INVALID_CPF",
                          "message": "CPF format is invalid",
                          "timestamp": "2025-09-19T10:30:00"
                        }
                        """
                    ),
                    @ExampleObject(
                        name = "CPF already exists",
                        value = """
                        {
                          "code": "CPF_ALREADY_EXISTS",
                          "message": "Member with this CPF already exists",
                          "timestamp": "2025-09-19T10:30:00"
                        }
                        """
                    )
                }
            )
        )
    })
    public ResponseEntity<Member> createMember(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Member creation details",
                required = true,
                content = @Content(
                    examples = @ExampleObject(
                        name = "Create member example",
                        value = """
                        {
                          "cpf": "12345678901",
                          "name": "João Silva"
                        }
                        """
                    )
                )
            )
            @Valid @RequestBody CreateMemberRequest request) {
        Member member = memberService.createMember(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(member);
    }
    
    @GetMapping("/v1/{id}")
    @Operation(summary = "Get member by ID", description = "Retrieve member details by unique identifier")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Member found",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Member.class),
                examples = @ExampleObject(
                    value = """
                    {
                      "id": "123e4567-e89b-12d3-a456-426614174000",
                      "cpf": "12345678901",
                      "name": "João Silva",
                      "createdAt": "2025-09-19T10:30:00"
                    }
                    """
                )
            )
        ),
        @ApiResponse(
            responseCode = "404", 
            description = "Member not found",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = """
                    {
                      "code": "MEMBER_NOT_FOUND",
                      "message": "Member not found with ID: 123e4567-e89b-12d3-a456-426614174000",
                      "timestamp": "2025-09-19T10:30:00"
                    }
                    """
                )
            )
        )
    })
    public ResponseEntity<Member> getMemberById(
            @Parameter(
                description = "Member unique identifier", 
                example = "123e4567-e89b-12d3-a456-426614174000"
            ) 
            @PathVariable UUID id) {
        Member member = memberService.getMemberById(id);
        return ResponseEntity.ok(member);
    }
    
    @GetMapping("/cpf/v1/{cpf}")
    @Operation(summary = "Get member by CPF", description = "Retrieve member details by Brazilian CPF")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Member found",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Member.class),
                examples = @ExampleObject(
                    value = """
                    {
                      "id": "123e4567-e89b-12d3-a456-426614174000",
                      "cpf": "12345678901",
                      "name": "João Silva",
                      "createdAt": "2025-09-19T10:30:00"
                    }
                    """
                )
            )
        ),
        @ApiResponse(
            responseCode = "404", 
            description = "Member not found",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = """
                    {
                      "code": "MEMBER_NOT_FOUND",
                      "message": "Member not found with CPF: 12345678901",
                      "timestamp": "2025-09-19T10:30:00"
                    }
                    """
                )
            )
        )
    })
    public ResponseEntity<Member> getMemberByCpf(
            @Parameter(
                description = "Member CPF (11 digits)", 
                example = "12345678901"
            ) 
            @PathVariable String cpf) {
        Member member = memberService.getMemberByCpf(cpf);
        return ResponseEntity.ok(member);
    }
    
    @GetMapping("/v1")
    @Operation(summary = "Get all members", description = "Retrieve all registered cooperative members")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "List of all members",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = """
                    [
                      {
                        "id": "123e4567-e89b-12d3-a456-426614174000",
                        "cpf": "12345678901",
                        "name": "João Silva",
                        "createdAt": "2025-09-19T10:30:00"
                      },
                      {
                        "id": "456e7890-e89b-12d3-a456-426614174001",
                        "cpf": "98765432109",
                        "name": "Maria Santos",
                        "createdAt": "2025-09-19T11:00:00"
                      }
                    ]
                    """
                )
            )
        )
    })
    public ResponseEntity<List<Member>> getAllMembers() {
        List<Member> members = memberService.getAllMembers();
        return ResponseEntity.ok(members);
    }
}