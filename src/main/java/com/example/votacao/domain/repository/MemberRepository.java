package com.example.votacao.domain.repository;

import com.example.votacao.domain.model.Member;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MemberRepository {
    Member save(Member member);
    Optional<Member> findById(UUID id);
    Optional<Member> findByCpf(String cpf);
    List<Member> findAll();
    void delete(Member member);
    boolean existsById(UUID id);
    boolean existsByCpf(String cpf);
}