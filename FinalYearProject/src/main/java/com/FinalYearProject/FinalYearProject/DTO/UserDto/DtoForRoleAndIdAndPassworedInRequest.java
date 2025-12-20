package com.FinalYearProject.FinalYearProject.DTO.UserDto;

/**
 * DTO for Role Updates by ID with Authorization
 * PURPOSE: Data Transfer Object for changing user roles identified by database ID, requiring password verification for secure authorization of privilege changes.
 * EFFICIENT IDENTIFICATION: Uses database primary key (id) for precise user targeting, faster than email lookups and immune to email changes.
 * ROLE CHANGE SECURITY: password field requires verification - typically admin password for admin-driven changes, but could be user password for self-service upgrades.
 * USAGE CONTEXT: Used in admin controller endpoint updateUserRoleById. Provides secure role management with efficient user identification.
 * PERFORMANCE BENEFITS: ID lookups use primary key index for maximum database efficiency. Particularly valuable for frequent role management operations.
 * SECURITY LAYERING: Combines JWT role validation with password re-authentication for high-privilege operations. Defense in depth against token compromise.
 * VALIDATION REQUIREMENTS: Must validate that ID exists, role is valid system role, and password matches authorized user (admin or target user).
 * AUDIT TRAIL: Creates comprehensive audit record including admin/user who authorized change, target user ID, old role, new role, and timestamp.
 * INTEGRATION: Works with UserService.updateUserRoleById() method. Service should handle authorization verification and audit logging.
 * SERIALIZATION: Three-field POJO for JSON requests. Clear distinction between target (id), change (role), and authorization (password).
 */
public class DtoForRoleAndIdAndPassworedInRequest {
    Long id;
    String role;
    String password;

    public DtoForRoleAndIdAndPassworedInRequest() {}

    public DtoForRoleAndIdAndPassworedInRequest(Long id, String role,String password){
        this.role=role;
        this.id=id;
        this.password=password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
