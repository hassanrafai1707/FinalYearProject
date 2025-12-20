package com.FinalYearProject.FinalYearProject.Exceptions.UserEeceptions;

/**
 * WrongPasswordException - Credential Validation Exception
 * PURPOSE: Custom runtime exception thrown when password validation fails during authentication or authorization, indicating incorrect credentials.
 * EXCEPTION TYPE: Unchecked (RuntimeException) as wrong password represents an authentication failure that should interrupt normal flow and trigger security responses.
 * TRIGGER CONDITIONS: Thrown when password doesn't match stored hash during login, admin password verification, or sensitive operations requiring re-authentication.
 * SECURITY FUNCTION: Part of authentication security - prevents unauthorized access even with valid username/email. Used in UserService.verifyLoginByEmail() and admin verification methods.
 * ERROR HANDLING: Results in HTTP 401 Unauthorized response. Should use generic messaging like "Invalid credentials" to avoid revealing whether username exists.
 * MESSAGE STRATEGY: Security best practice dictates consistent messaging for both wrong password and non-existent user to prevent username enumeration attacks.
 * USAGE CONTEXT: Primary use in authentication endpoints (login). Also in admin operations requiring password verification (delete, suspend, role changes).
 * SECURITY INTEGRATION: Should trigger failed attempt tracking for account lockout mechanisms. Part of defense against credential stuffing and brute force attacks.
 * INTEGRATION: Extends RuntimeException for Spring Security compatibility. Works with authentication providers and credential validation services.
 */
public class WrongPasswordException extends RuntimeException {
    public WrongPasswordException(String message) {
        super(message);
    }
}
