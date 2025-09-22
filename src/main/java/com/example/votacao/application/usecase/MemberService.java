package com.example.votacao.application.usecase;

import com.example.votacao.application.dto.CreateMemberRequest;
import com.example.votacao.domain.model.Member;
import com.example.votacao.domain.repository.MemberRepository;
import com.example.votacao.domain.service.CpfValidationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class MemberService {
    
    private final MemberRepository memberRepository;
    private final CpfValidationService cpfValidationService;
    
    public Member createMember(CreateMemberRequest request) {
        // Validate CPF with external service
        cpfValidationService.validateCpf(request.getCpf());
        
        if (memberRepository.existsByCpf(request.getCpf())) {
            throw new IllegalArgumentException("Member with CPF " + request.getCpf() + " already exists");
        }
        
        Member member = new Member(request.getCpf(), request.getName());
        return memberRepository.save(member);
    }
    
    @Transactional(readOnly = true)
    public Member getMemberById(UUID id) {
        return memberRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Member not found with id: " + id));
    }
    
    @Transactional(readOnly = true)
    public Member getMemberByCpf(String cpf) {
        return memberRepository.findByCpf(cpf)
                .orElseThrow(() -> new IllegalArgumentException("Member not found with CPF: " + cpf));
    }
    
    @Transactional(readOnly = true)
    public List<Member> getAllMembers() {
        return memberRepository.findAll();
    }
}