package com.example.votacao.domain.model;

import lombok.Getter;

@Getter
public enum VoteType {
    YES("Yes"),
    NO("No");

    private final String value;

    VoteType(String value) {
        this.value = value;
    }
}