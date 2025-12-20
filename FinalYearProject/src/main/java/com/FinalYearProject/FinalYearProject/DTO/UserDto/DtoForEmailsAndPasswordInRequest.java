package com.FinalYearProject.FinalYearProject.DTO.UserDto;

import java.util.ArrayList;
import java.util.List;

/**
 * DTO for Batch Email Operations with Admin Authorization
 * PURPOSE: Data Transfer Object for bulk user operations targeting multiple email addresses, requiring administrator password verification for batch authorization.
 * BATCH OPERATIONS: emails list contains multiple target user emails for simultaneous processing. Supports efficient bulk actions like mass deletion, suspension, or updates.
 * SECURITY MODEL: Requires admin password verification for batch operations due to higher impact. Single admin authorization covers multiple user modifications.
 * USAGE CONTEXT: Used in admin batch deletion endpoints (deleteUsersInBatchByEmail). Enables administrators to process multiple users efficiently while maintaining security controls.
 * PERFORMANCE CONSIDERATIONS: Batch operations reduce database round-trips compared to individual API calls. Should include limits on batch size to prevent abuse.
 * VALIDATION: Should validate that emails list is not empty, contains valid email formats, and all emails exist in system. Admin password must match requesting admin's credentials.
 * ERROR HANDLING: Batch operations should use transactional processing where possible. Partial failures should be handled gracefully with clear error reporting.
 * INTEGRATION: Used with UserService.deleteUserInBatchEmail() method. Service layer should implement proper transaction management for batch operations.
 * SECURITY AUDIT: Provides clear audit trail showing which admin performed batch operation, which users were affected, and when operation occurred.
 * SERIALIZATION: Contains List<String> for emails and single adminPassword. Enables clean JSON structure for batch operation requests.
 */
public class DtoForEmailsAndPasswordInRequest {
    List<String> emails=new ArrayList<>();
    String adminPassword;

    public DtoForEmailsAndPasswordInRequest() {}

    public DtoForEmailsAndPasswordInRequest(String adminPassword,List<String> emails){
        this.adminPassword=adminPassword;
        this.emails=emails;
    }

    public List<String> getEmails() {
        return emails;
    }

    public void setEmails(List<String> emails) {
        this.emails = emails;
    }

    public String getAdminPassword() {
        return adminPassword;
    }

    public void setAdminPassword(String adminPassword) {
        this.adminPassword = adminPassword;
    }
}
