package com.FinalYearProject.FinalYearProject.DTO.UserDto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * DTO for Batch ID Operations with Admin Authorization
 * PURPOSE: Data Transfer Object for bulk user operations targeting multiple database IDs, requiring administrator password verification for batch authorization security.
 * BATCH ID PROCESSING: ids list contains multiple user database primary keys for efficient batch operations. Enables processing many users in single transaction.
 * PERFORMANCE OPTIMIZATION: ID-based batch operations are highly efficient (primary key lookups). Reduces database load compared to individual API calls.
 * SECURITY REQUIREMENT: adminPassword verification ensures batch operations are intentionally authorized by admin, not just using stolen JWT token.
 * USAGE CONTEXT: Used in admin batch deletion endpoint (deleteUsersInBatchByID). Supports efficient user management for large-scale operations.
 * TRANSACTION MANAGEMENT: Batch operations should be wrapped in transactions to ensure atomicity - either all succeed or all fail rollback.
 * VALIDATION: Should validate that all IDs exist, list is not empty, and has reasonable size limit. Admin password must be verified.
 * ERROR HANDLING: Should implement partial success handling or full rollback. Consider implementing batch size limits to prevent system overload.
 * INTEGRATION: Used with UserService.deleteUserInBatchById() method. Service should handle database efficiency and error scenarios.
 * AUDIT TRAIL: Provides clear record of which admin performed batch operation, which user IDs were affected, and operation timestamp.
 * SERIALIZATION: Contains List<Long> for IDs and single adminPassword. Enables efficient JSON structure for batch operation requests.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DtoForIdsAndPasswordInRequest {
    List<Long> ids;
    String adminPassword;
}
