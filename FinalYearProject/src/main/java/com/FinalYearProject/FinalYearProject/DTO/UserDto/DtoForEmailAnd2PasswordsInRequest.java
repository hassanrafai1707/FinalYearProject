package com.FinalYearProject.FinalYearProject.DTO.UserDto;

/**
 * DTO for Email and Dual Password Operations
 * PURPOSE: Data Transfer Object for operations requiring user email and two different passwords - typically user password and admin password for authorization verification.
 * PASSWORD DISTINCTION: password - user's own password (for self-service operations). adminPassword - administrator password (for privileged operations requiring extra verification).
 * USAGE SCENARIOS: User password reset by admin (requires admin verification). User role changes by admin. Sensitive user modifications requiring dual authorization.
 * SECURITY MODEL: Implements two-factor authorization concept for sensitive operations. Admin must provide their own password as additional security measure beyond role-based access.
 * VALIDATION: Both passwords should be validated - user password against user's stored hash, admin password against requesting admin's stored hash.
 * INTEGRATION: Used in admin controller endpoints for user management operations (updateUserPasswordByEmail, etc.). Ensures admin actions are intentionally authorized.
 * AUDIT TRAIL: Provides clear record of who authorized sensitive changes (admin identity) and which user was affected. Supports compliance and accountability requirements.
 * SERIALIZATION: Three-field POJO for JSON requests. Clear field naming distinguishes between user and admin credentials.
 * SECURITY BEST PRACTICE: Admin password should never be logged. Should be validated quickly and discarded from memory. Password fields should use char[] in future for better security.
 */
public class DtoForEmailAnd2PasswordsInRequest {
    String email;
    String password;
    String adminPassword;

    public DtoForEmailAnd2PasswordsInRequest(){}

    public DtoForEmailAnd2PasswordsInRequest(String email, String password , String adminPassword){
        this.email=email;
        this.password=password;
        this.adminPassword=adminPassword;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getAdminPassword() {
        return adminPassword;
    }

    public void setAdminPassword(String adminPassword) {
        this.adminPassword = adminPassword;
    }
}
