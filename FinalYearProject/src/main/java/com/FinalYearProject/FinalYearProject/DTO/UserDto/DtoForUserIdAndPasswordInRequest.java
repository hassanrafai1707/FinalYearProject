package com.FinalYearProject.FinalYearProject.DTO.UserDto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for User Password Updates by ID with Dual Authorization
 * PURPOSE: Data Transfer Object for changing user passwords identified by database ID, requiring both new password and admin password verification for secure password reset operations.
 * DUAL AUTHORIZATION MODEL: password - new password to set for target user. adminPassword - requesting admin's password for authorization verification. Combines change specification with security authorization.
 * ID-BASED TARGETING: Uses database primary key (id) for efficient and unambiguous user identification. Avoids email ambiguity and provides better performance.
 * SECURITY WORKFLOW: Admin must provide their own password (beyond JWT) to authorize password reset, preventing misuse of admin privileges if token is compromised.
 * USAGE CONTEXT: Used in admin controller endpoint updateUserPasswordById. Enables administrators to reset user passwords with proper authorization controls.
 * PASSWORD SECURITY: New password should meet complexity requirements. Service should hash password using BCrypt before storage. Should not log or expose passwords.
 * AUDIT REQUIREMENTS: Password resets should be thoroughly logged including which admin performed reset, which user was affected, timestamp, and IP address.
 * INTEGRATION: Used with UserService.updateUserPasswordById() method. Service should validate admin password, hash new password, update user, and log event.
 * ERROR HANDLING: Should handle invalid user ID, weak new password, incorrect admin password, and system errors gracefully.
 * SERIALIZATION: Three-field POJO for JSON requests. Clear separation between target user (id), new credential (password), and authorization (adminPassword).
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DtoForUserIdAndPasswordInRequest {
    Long id;
    String password;
    String adminPassword;
}
