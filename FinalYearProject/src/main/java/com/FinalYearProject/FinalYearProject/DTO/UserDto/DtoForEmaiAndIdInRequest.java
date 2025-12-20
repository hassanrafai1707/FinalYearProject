package com.FinalYearProject.FinalYearProject.DTO.UserDto;

/**
 * DTO for User Identification by Email or ID
 * PURPOSE: Data Transfer Object that provides dual identification options for user operations - can specify user by either database ID or email address. Supports flexible user lookup in admin operations.
 * DUAL IDENTIFICATION STRATEGY: Contains both Id (database primary key) and email fields. Allows API consumers to use whichever identifier they have available.
 * USAGE CONTEXT: Used in administrative endpoints where user identification is required but the caller may have either ID or email. Supports failover or preference-based identification.
 * NULL HANDLING: Both fields can be null - validation should occur at service layer to ensure at least one identifier is provided. Service methods should prioritize ID over email for performance.
 * INTEGRATION: Used by UserService methods that accept either identifier type. Simplifies API design by providing single DTO for multiple identification scenarios.
 * SECURITY CONSIDERATIONS: When used in admin operations, should validate that requesting admin has appropriate privileges regardless of identification method used.
 * SERIALIZATION: Simple POJO for JSON request bodies. Both fields optional but at least one required. Enables clean API contracts for user lookup operations.
 * EXTENSION: Could be enhanced with additional identification methods (username, employee ID, student ID) for institutions with multiple identification systems.
 */
public class DtoForEmaiAndIdInRequest {
    Long Id;
    String email;

    public DtoForEmaiAndIdInRequest(){}

    public DtoForEmaiAndIdInRequest(Long Id, String email){
        this.Id=Id;
        this.email=email;
    }

    public void setId(Long Id){
        this.Id=Id;
    }

    public void setEmail(String email){
        this.email=email;
    }
    public Long getId(){
        return Id;
    }

    public String getEmail(){
        return email;
    }
}
