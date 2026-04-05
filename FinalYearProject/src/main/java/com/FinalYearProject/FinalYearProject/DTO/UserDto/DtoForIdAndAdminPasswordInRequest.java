package com.FinalYearProject.FinalYearProject.DTO.UserDto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for ID-Based Operations with Admin Authorization
 * PURPOSE: Data Transfer Object for user operations identified by database ID that require administrator password verification for extra security authorization.
 * ID-BASED IDENTIFICATION: Uses database primary key (id) for precise user targeting. More efficient than email lookups and avoids email change issues.
 * SECURITY LAYER: adminPassword field requires requesting admin to re-authenticate with their current password, adding defense-in-depth beyond JWT role validation.
 * USAGE CONTEXT: Used in admin controller endpoints for single-user operations by ID (deleteUserById, suspendUserById, etc.). Provides efficient and secure user management.
 * PERFORMANCE ADVANTAGE: ID lookups are faster than email lookups (primary key vs indexed column). More reliable for programmatic operations.
 * INTEGRATION: Works with UserService methods that accept user IDs. Service layer validates admin password before executing any changes.
 * ERROR PREVENTION: ID-based operations avoid ambiguity with duplicate emails or email format issues. Provides unambiguous user identification.
 * SECURITY AUDIT: Creates clear audit trail with admin identity verification via password, target user ID, and timestamp of operation.
 * SERIALIZATION: Simple two-field POJO for JSON requests. Clear distinction between target user (id) and authorizing admin (adminPassword).
 * VALIDATION: Should validate that ID exists and belongs to valid user. Admin password must be verified against stored hash using secure comparison.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DtoForIdAndAdminPasswordInRequest {
    Long id;
    String adminPassword;
}
