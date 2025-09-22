package com.example.votacao.domain.service;

import com.example.votacao.domain.model.CpfValidationResponse;

public interface CpfValidationService {
    CpfValidationResponse validateCpf(String cpf);
}