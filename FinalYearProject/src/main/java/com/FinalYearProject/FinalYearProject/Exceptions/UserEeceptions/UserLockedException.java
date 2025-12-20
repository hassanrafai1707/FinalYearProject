package com.FinalYearProject.FinalYearProject.Exceptions.UserEeceptions;

/**
 * UserLockedException - Account Security Exception
 * PURPOSE: Custom runtime exception thrown when authentication is attempted for a locked user account, indicating temporary security lockout typically due to failed login attempts.
 * EXCEPTION TYPE: Unchecked (RuntimeException) as account lock represents a security state that prevents normal authentication flow and requires special handling.
 * TRIGGER CONDITIONS: Thrown during login attempt (AuthRestController) when User.locked field is true. Used in UserService.verifyLoginByEmail() and authentication providers.
 * SECURITY MECHANISM: Part of account protection against brute force attacks. Temporary lockout after consecutive failed login attempts (implemented in service layer).
 * ERROR HANDLING: Results in HTTP 423 Locked or 401 Unauthorized response. Should include lock duration information if available (e.g., "Account locked for 15 minutes").
 * MESSAGE CONTEXT: Should inform user about lock status without revealing security details. Could provide unlock instructions (wait, contact admin, password reset).
 * USAGE CONTEXT: Primarily in authentication flows. Also relevant in admin user management when checking account status.
 * SECURITY LOGGING: Should trigger security audit logging for suspicious activity. May integrate with intrusion detection systems.
 * INTEGRATION: Extends RuntimeException for Spring Security integration. Works with authentication providers and custom UserDetailsService implementations.
 */
public class UserLockedException extends RuntimeException {
    public UserLockedException(String message) {
        super(message);
    }
}
