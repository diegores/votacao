package com.example.votacao.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "members", indexes = {
    @Index(name = "idx_member_cpf", columnList = "cpf", unique = true),
    @Index(name = "idx_member_name", columnList = "name")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Member {
    
    @Id
    private UUID id;
    
    @Column(nullable = false, unique = true)
    private String cpf;
    
    @Column(nullable = false)
    private String name;
    
    public Member(String cpf, String name) {
        this.id = UUID.randomUUID();
        this.cpf = cpf;
        this.name = name;
    }
}