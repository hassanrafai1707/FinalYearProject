package com.FinalYearProject.FinalYearProject.Exceptions.UserEeceptions;

/**
 * UserNotAuthorizesException - Authorization Failure Exception
 * PURPOSE: Custom runtime exception thrown when a user attempts an operation without sufficient permissions or authorization, enforcing role-based access control.
 * EXCEPTION TYPE: Unchecked (RuntimeException) following Spring Security patterns where authorization failures are unrecoverable without privilege elevation.
 * TRIGGER CONDITIONS: Thrown when user lacks required authority for requested operation. Used in service layer authorization checks before performing sensitive operations.
 * AUTHORIZATION ENFORCEMENT: Complementary to Spring Security method security (@PreAuthorize). Provides programmatic authorization checks in business logic.
 * ERROR HANDLING: Results in HTTP 403 Forbidden response. Global exception handler provides generic "access denied" message without revealing system details.
 * MESSAGE CONTENT: Should be generic for security - e.g., "Insufficient permissions" rather than "You need ROLE_ADMIN". Avoids exposing role hierarchy.
 * USAGE CONTEXT: Used in UserService for admin password verification failures, role change validations, and sensitive operations requiring extra authorization.
 * SECURITY LAYERING: Adds business logic authorization beyond endpoint security. Ensures even if endpoint is accessed, operation-specific checks still apply.
 * INTEGRATION: Extends RuntimeException for Spring compatibility. Works alongside Spring Security's AccessDeniedException for comprehensive authorization.
 */
public class UserNotAuthorizesException extends RuntimeException {
    public UserNotAuthorizesException(String message) {
        super(message);
    }
}
