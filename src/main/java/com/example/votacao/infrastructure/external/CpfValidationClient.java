package com.example.votacao.infrastructure.external;

import com.example.votacao.domain.model.CpfValidationResponse;
import com.example.votacao.domain.model.CpfValidationStatus;
import com.example.votacao.domain.service.CpfValidationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
@Slf4j
public class CpfValidationClient implements CpfValidationService {
    
    private final Random random = new Random();
    
    @Override
    public CpfValidationResponse validateCpf(String cpf) {
        log.info("Validating CPF: {}", maskCpf(cpf));
        
        // Validate CPF format and checksum
        if (!isValidCpfFormat(cpf)) {
            log.warn("Invalid CPF format: {}", maskCpf(cpf));
            throw new CpfNotFoundException("CPF not found: " + maskCpf(cpf));
        }
        
        // Simulate external service call with random response
        boolean canVote = random.nextBoolean();
        CpfValidationStatus status = canVote ? CpfValidationStatus.ABLE_TO_VOTE : CpfValidationStatus.UNABLE_TO_VOTE;
        
        log.info("CPF {} validation result: {}", maskCpf(cpf), status);
        
        if (status == CpfValidationStatus.UNABLE_TO_VOTE) {
            throw new CpfUnableToVoteException("CPF Já existente na base: " + maskCpf(cpf));
        }
        
        return new CpfValidationResponse(status);
    }
    
    private boolean isValidCpfFormat(String cpf) {
        // Remove any non-digit characters
        cpf = cpf.replaceAll("\\D", "");
        
        // Check if CPF has 11 digits
        if (cpf.length() != 11) {
            return false;
        }
        
        // Check if all digits are the same (invalid CPFs)
        if (cpf.matches("(\\d)\\1{10}")) {
            return false;
        }
        
        // Validate CPF checksum
        return isValidCpfChecksum(cpf);
    }
    
    private boolean isValidCpfChecksum(String cpf) {
        try {
            // Calculate first check digit
            int sum = 0;
            for (int i = 0; i < 9; i++) {
                sum += Character.getNumericValue(cpf.charAt(i)) * (10 - i);
            }
            int firstCheckDigit = (sum % 11 < 2) ? 0 : 11 - (sum % 11);
            
            // Calculate second check digit
            sum = 0;
            for (int i = 0; i < 10; i++) {
                sum += Character.getNumericValue(cpf.charAt(i)) * (11 - i);
            }
            int secondCheckDigit = (sum % 11 < 2) ? 0 : 11 - (sum % 11);
            
            // Verify check digits
            return Character.getNumericValue(cpf.charAt(9)) == firstCheckDigit &&
                   Character.getNumericValue(cpf.charAt(10)) == secondCheckDigit;
        } catch (Exception e) {
            return false;
        }
    }
    
    private String maskCpf(String cpf) {
        if (cpf == null || cpf.length() < 11) {
            return "***.***.**-**";
        }
        cpf = cpf.replaceAll("\\D", "");
        return cpf.substring(0, 3) + ".***.**-" + cpf.substring(9);
    }
    
    public static class CpfNotFoundException extends RuntimeException {
        public CpfNotFoundException(String message) {
            super(message);
        }
    }
    
    public static class CpfUnableToVoteException extends RuntimeException {
        public CpfUnableToVoteException(String message) {
            super(message);
        }
    }
}