package com.FinalYearProject.FinalYearProject.DTO.UserDto;

/**
 * DTO for Email-Based Operations with Admin Authorization
 * PURPOSE: Data Transfer Object for user operations identified by email that require administrator password verification for authorization.
 * SECURITY MODEL: Implements admin re-authentication requirement for sensitive operations. Admin must provide their current password to authorize changes, adding extra security layer beyond JWT role validation.
 * USAGE CONTEXT: Used in admin controller delete and suspend operations (deleteUserByEmail, suspendUserByEmail, etc.). Prevents misuse of admin privileges if token is compromised.
 * FIELD DEFINITIONS: email - target user's email for operation. adminPassword - requesting admin's current password for verification.
 * AUTHORIZATION WORKFLOW: 1. JWT validates admin role. 2. Admin password verified against stored hash. 3. Operation proceeds only if both checks pass.
 * SECURITY BENEFITS: Defense in depth - even with valid admin JWT, attacker needs admin password. Reduces risk of token theft leading to unauthorized changes. Supports principle of least privilege.
 * INTEGRATION: Used with UserService methods that perform destructive operations. Service layer validates admin password before executing changes.
 * AUDIT VALUE: Creates clear audit trail showing which admin authorized specific operation with explicit password verification.
 * SERIALIZATION: Simple two-field POJO for JSON requests. Clear separation between target user (email) and authorizing admin (adminPassword).
 * SECURITY NOTE: Admin password should be validated quickly using secure hash comparison. Should not be logged or stored beyond verification.
 */
public class DtoForEmailAndAdminPasswordInRequest {
    String email;
    String adminPassword;

    public DtoForEmailAndAdminPasswordInRequest(){}

    public DtoForEmailAndAdminPasswordInRequest(String email,String adminPassword){
        this.email=email;
        this.adminPassword=adminPassword;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAdminPassword() {
        return adminPassword;
    }

    public void setAdminPassword(String adminPassword) {
        this.adminPassword = adminPassword;
    }
}
