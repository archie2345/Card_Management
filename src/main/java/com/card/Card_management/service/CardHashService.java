package com.card.Card_management.service;

import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

@Service
public class CardHashService {

    private static final HexFormat HEX = HexFormat.of();
    private final MessageDigest digest;

    public CardHashService() {
        try {
            this.digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm not available", e);
        }
    }

    public String hashLastFour(String lastFourDigits) {
        byte[] input = lastFourDigits.getBytes(StandardCharsets.UTF_8);
        byte[] hashed = digest.digest(input);
        return HEX.formatHex(hashed);
    }
}
