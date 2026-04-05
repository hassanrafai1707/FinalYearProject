package com.FinalYearProject.FinalYearProject.Domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.Random;
import java.util.UUID;

/**
 * Conformation Domain Class for User Email Verification
 * PURPOSE: Manages email verification tokens and OTPs for user account confirmation. Used during registration process to validate user email addresses before account activation.
 * TOKEN GENERATION: Uses UUID.randomUUID() to create unique verification tokens. Tokens are included in verification emails sent to users.
 * OTP GENERATION: Generates 4-digit random OTPs (1000-9999) using Random.nextInt(). Provides second factor for verification (token + OTP).
 * USER ASSOCIATION: Each Conformation object is linked to a specific User entity. Tracks which user needs verification.
 * CONSTRUCTION: Parameterized constructor accepts User object and automatically generates token and OTP. No-args constructor for persistence frameworks.
 * SECURITY CONSIDERATIONS: UUID tokens provide sufficient entropy against brute force. 4-digit OTPs offer reasonable security for email verification. Tokens should have expiration time (handled at service layer).
 * USAGE FLOW: 1. User registers -> Conformation object created with token+OTP. 2. Email sent with token and OTP. 3. User clicks link/enters OTP -> system validates both. 4. Account activated on successful verification.
 * BUILDER PATTERN: Uses @SuperBuilder for flexible object creation. Lombok annotations reduce boilerplate code (@Getter, @Setter, @AllArgsConstructor, @NoArgsConstructor).
 */
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@Getter
@Setter
public class Conformation {
    public String token;
    public int Otp;
    private User user;

    public Conformation(User user) {
        this.user = user;
        this.token= UUID.randomUUID().toString();
        Random random=new Random();
        this.Otp= random.nextInt(1000,9999);
    }
}
