package com.example.votacao.infrastructure.util;

import java.util.Random;

public class CpfGenerator {
    
    private static final Random random = new Random();
    
    public static String generateValidCpf() {
        // Generate first 9 digits
        StringBuilder cpf = new StringBuilder();
        for (int i = 0; i < 9; i++) {
            cpf.append(random.nextInt(10));
        }
        
        // Calculate and append check digits
        String cpfBase = cpf.toString();
        int firstCheckDigit = calculateCheckDigit(cpfBase, 10);
        int secondCheckDigit = calculateCheckDigit(cpfBase + firstCheckDigit, 11);
        
        cpf.append(firstCheckDigit).append(secondCheckDigit);
        return cpf.toString();
    }
    
    public static String generateFormattedValidCpf() {
        String cpf = generateValidCpf();
        return cpf.substring(0, 3) + "." + 
               cpf.substring(3, 6) + "." + 
               cpf.substring(6, 9) + "-" + 
               cpf.substring(9, 11);
    }
    
    private static int calculateCheckDigit(String cpf, int weight) {
        int sum = 0;
        for (int i = 0; i < cpf.length(); i++) {
            sum += Character.getNumericValue(cpf.charAt(i)) * (weight - i);
        }
        int remainder = sum % 11;
        return remainder < 2 ? 0 : 11 - remainder;
    }
    
    // Generate some sample valid CPFs for testing
    public static String[] getSampleValidCpfs() {
        return new String[]{
                "11144477735",
                "22255588820",
                "33366699914",
                "44477700828",
                "55588811743"
        };
    }
}