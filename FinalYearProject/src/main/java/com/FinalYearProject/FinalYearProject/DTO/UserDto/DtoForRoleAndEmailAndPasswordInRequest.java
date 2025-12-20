package com.FinalYearProject.FinalYearProject.DTO.UserDto;

/**
 * DTO for Role Updates by Email with Authorization
 * PURPOSE: Data Transfer Object for changing user roles identified by email, requiring password verification for authorization (either user or admin password depending on context).
 * ROLE MANAGEMENT: role - new role/authority to assign to user (e.g., "ROLE_STUDENT", "ROLE_TEACHER"). email - target user's email for role update.
 * AUTHORIZATION MECHANISM: password field provides security verification - could be user's password (for self-upgrade requests) or admin password (for admin-driven role changes).
 * USAGE CONTEXT: Used in admin controller endpoint updateUserRoleByEmail. Could also be used for user role upgrade requests with self-verification.
 * SECURITY IMPLICATIONS: Role changes are high-privilege operations. Admin password verification adds extra security layer beyond JWT role validation.
 * VALIDATION: Should validate that role is valid system role, email exists, and password matches appropriate user (target user or admin).
 * AUDIT REQUIREMENTS: Role changes should be thoroughly logged including who changed, whose role changed, old role, new role, and timestamp.
 * INTEGRATION: Used with UserService.updateUserRoleByEmail() method. Service should verify authorization and log the role change event.
 * ERROR HANDLING: Should handle invalid roles, non-existent users, incorrect passwords, and insufficient privileges gracefully.
 * SERIALIZATION: Three-field POJO for JSON requests. Clear separation between target user (email), new privilege (role), and authorization (password).
 */
public class DtoForRoleAndEmailAndPasswordInRequest {
    String email;
    String role;
    String password;

    public DtoForRoleAndEmailAndPasswordInRequest() {}

    public DtoForRoleAndEmailAndPasswordInRequest(String email, String role, String password){
        this.email=email;
        this.role=role;
        this.password=password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
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
}
