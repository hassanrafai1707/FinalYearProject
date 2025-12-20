package com.FinalYearProject.FinalYearProject.Exceptions.UserEeceptions;

/**
 * UserNotFoundException - User Lookup Exception
 * PURPOSE: Custom runtime exception thrown when a requested user cannot be found in the system, indicating invalid user reference or non-existent account.
 * EXCEPTION TYPE: Unchecked (RuntimeException) following Spring conventions for resource not found scenarios that should map to HTTP 404 responses.
 * TRIGGER CONDITIONS: Thrown when user lookup by ID, email, or other identifier returns null. Used across UserService methods like findUserById(), findByEmail(), update methods, etc.
 * ERROR HANDLING: Caught by global @ControllerAdvice exception handler and converted to HTTP 404 Not Found with appropriate error message.
 * MESSAGE CONTENT: Should include identifying information for debugging while maintaining security - e.g., "User not found" rather than "User with email hacker@test.com not found".
 * USAGE CONTEXT: Used across all controllers (admin, auth, etc.) when referencing non-existent users. Critical for preventing operations on invalid user references.
 * SECURITY CONSIDERATIONS: Should use consistent messaging and timing to prevent user enumeration attacks. Avoid revealing whether specific emails exist in system.
 * INTEGRATION: Extends RuntimeException for Spring compatibility. Can be used with @ResponseStatus(HttpStatus.NOT_FOUND) for automatic HTTP status mapping.
 * RELATED PATTERNS: Often used with Optional return types in repository layer, with exception thrown at service layer for "must exist" scenarios.
 */
public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException(String message) {
        super(message);
    }

}
