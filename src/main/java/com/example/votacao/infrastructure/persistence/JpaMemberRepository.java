package com.example.votacao.infrastructure.persistence;

import com.example.votacao.domain.model.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface JpaMemberRepository extends JpaRepository<Member, UUID> {
    Optional<Member> findByCpf(String cpf);
    boolean existsByCpf(String cpf);
}