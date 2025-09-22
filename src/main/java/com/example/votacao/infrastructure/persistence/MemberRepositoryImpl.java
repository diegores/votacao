package com.example.votacao.infrastructure.persistence;

import com.example.votacao.domain.model.Member;
import com.example.votacao.domain.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class MemberRepositoryImpl implements MemberRepository {
    
    private final JpaMemberRepository jpaRepository;
    
    @Override
    public Member save(Member member) {
        return jpaRepository.save(member);
    }
    
    @Override
    public Optional<Member> findById(UUID id) {
        return jpaRepository.findById(id);
    }
    
    @Override
    public Optional<Member> findByCpf(String cpf) {
        return jpaRepository.findByCpf(cpf);
    }
    
    @Override
    public List<Member> findAll() {
        return jpaRepository.findAll();
    }
    
    @Override
    public void delete(Member member) {
        jpaRepository.delete(member);
    }
    
    @Override
    public boolean existsById(UUID id) {
        return jpaRepository.existsById(id);
    }
    
    @Override
    public boolean existsByCpf(String cpf) {
        return jpaRepository.existsByCpf(cpf);
    }
}