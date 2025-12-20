package com.FinalYearProject.FinalYearProject.Exceptions.UserEeceptions;

/**
 * DuplicateEmailException - User Registration Integrity Exception
 * PURPOSE: Custom runtime exception thrown when attempting to register or update a user with an email that already exists in the system, enforcing unique email constraint.
 * EXCEPTION TYPE: Unchecked (RuntimeException) as duplicate email represents a business rule violation that requires user intervention to resolve.
 * TRIGGER CONDITIONS: Thrown during user registration (AuthRestController) or email update when email uniqueness check fails. Used in UserService.creatUser() and update methods.
 * UNIQUENESS ENFORCEMENT: Critical for system integrity - ensures each user has unique identity for authentication, communication, and data ownership.
 * ERROR HANDLING: Typically results in HTTP 409 Conflict or 400 Bad Request response. Global exception handler provides user-friendly message suggesting email change.
 * MESSAGE CONTENT: Should be generic for security ("Email already registered") rather than revealing whether email exists. Could include password reset suggestion.
 * USAGE CONTEXT: Primary use in registration endpoint. Also in admin user management and self-service email update features.
 * SECURITY CONSIDERATIONS: Avoid email enumeration attacks by using consistent timing and messaging regardless of whether email exists. Consider "email already registered" vs "invalid credentials" patterns.
 * INTEGRATION: Extends RuntimeException for Spring compatibility. Works with Spring Data @UniqueConstraint validation at entity level as backup enforcement.
 */
public class DuplicateEmailException extends RuntimeException {
    public DuplicateEmailException(String message) {
        super(message);
    }
}
