package com.FinalYearProject.FinalYearProject.Controller;

import com.FinalYearProject.FinalYearProject.DTO.UserDto.*;
import com.FinalYearProject.FinalYearProject.Exceptions.UserEeceptions.RoleNotValidException;
import com.FinalYearProject.FinalYearProject.Service.UserService;
import com.FinalYearProject.FinalYearProject.Util.ResponseUtility;
import org.springframework.data.web.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Admin REST Controller for User Management Operations
 * PURPOSE:
 * Provides comprehensive user administration endpoints for system administrators.
 * All endpoints are prefixed with versioned admin path (${app.version}/admin).
 * SECURITY CONTEXT:
 * - Requires ROLE_ADMIN authority (enforced via SecurityConfig)
 * - All endpoints require JWT authentication with admin privileges
 * - Destructive operations (deletions, role changes) require additional admin password verification
 * - Stateless architecture using JWT tokens (no session management)
 * USER MANAGEMENT OPERATIONS:
 * 1. USER RETRIEVAL:
 *    - findUserById: Retrieve user by database ID
 *    - findByEmail: Retrieve user by email address
 *    - listOfUserByRole: Retrieve all users with specific role (with and without pagination)
 *    - getAllUsers: Retrieve all users in system
 *    - getAllUsersPaged: Retrieve paginated list of users
 * 2. USER DELETION OPERATIONS:
 *    - deleteUserByEmail: Delete single user by email (requires admin password)
 *    - deleteUserById: Delete single user by ID (requires admin password)
 *    - deleteUsersInBatchByID: Bulk delete by list of IDs (requires admin password)
 *    - deleteUsersInBatchByEmail: Bulk delete by list of emails (requires admin password)
 * 3. USER SUSPENSION MANAGEMENT:
 *    - suspendUserById/ByEmail: Suspend user account (prevents login)
 *    - unsuspendUserById/ByEmail: Reactivate suspended account
 * 4. USER UPDATE OPERATIONS:
 *    - updateUserPasswordByEmail/ById: Reset user password (requires admin password)
 *    - updateUserRoleByEmail/ById: Change user role/authority (requires admin password)
 *    - updateUserEmail: Change authenticated admin's own email address
 *    - updateUserPassword: Change authenticated admin's own password
 * 5. UTILITY ENDPOINTS:
 *    - logout: Logout endpoint (JWT stateless - typically handled via token blacklisting)
 *    - test: Connectivity testing endpoint
 * REQUEST/RESPONSE PATTERN:
 * - All endpoints accept and return JSON
 * - Uses Data Transfer Objects (DTOs) for structured request validation
 * - Consistent JSON response format: {"status": "successful/error", "data": {...}, "message": "..."}
 * - Validation errors handled via global exception handler
 * - Custom exceptions (e.g., RoleNotValidException) for business rule violations
 * PAGINATION:
 * - Supports pagination for large datasets via pageNo and size parameters
 * - Default page size: 100 records
 * - Returns PagedModel for standardized pagination metadata
 * ROLE VALIDATION:
 * - Valid roles: ROLE_ADMIN, ROLE_TEACHER, ROLE_STUDENT, ROLE_SUPERVISOR
 * - Role validation performed at controller level for /users/role endpoints
 * - Invalid roles throw RoleNotValidException
 * SECURITY NOTES:
 * - Admin password verification adds defense-in-depth for critical operations
 * - JWT token validation occurs before controller method invocation
 * - No session management - logout typically implemented via token blacklisting
 * - Role-based access control enforced at method/endpoint level
 * PERFORMANCE CONSIDERATIONS:
 * - Pagination endpoints prevent memory issues with large user databases
 * - Batch operations reduce database round-trips for bulk actions
 * - Consider implementing caching for frequently accessed user data
 * - Database indexes recommended on email, id, and role fields
 * ERROR HANDLING:
 * - Standardized error responses via global exception handler
 * - Business logic exceptions provide clear user messages
 * - Input validation performed via DTO constraints
 * - HTTP status codes: 200 (success), 400 (bad request), 401 (unauthorized), 403 (forbidden), 404 (not found)
 * MAINTENANCE NOTES:
 * - DTO naming follows pattern: DtoFor[ParameterTypes]InRequest
 * - Service layer handles business logic and data access
 * - Controller focuses on HTTP handling and response formatting
 * - Consistent naming convention: [action]UserBy[Identifier]
 */
@RequestMapping("${app.version}/admin")
@RestController
public class AdminRestController {
    private final UserService userService;

    AdminRestController(UserService userService){
        this.userService=userService;
    }

    private LocalDateTime getNowTime(){
        return LocalDateTime.now();
    }

    @GetMapping("/user/id/{id}")
    public ResponseEntity<?> findUserById(@PathVariable("id") Long id){
        return ResponseUtility.responseTemplateForSingleData(
                "successful",
                userService.findUserById(id),
                "User with id: "+id,
                200
        );
    }

    @GetMapping("/user/email/{email}")
    public ResponseEntity<?> findByEmail(@PathVariable("email") String email) {
        return ResponseUtility.responseTemplateForSingleData(
                "successful",
                userService.findByEmail(email),
                "user with email: "+email,
                200
        );
    }

    @GetMapping("/users/role/{role}")
    public ResponseEntity<?> listOfUserByRole(@PathVariable("role") String role){
        if (
                !(
                        role.equals("ROLE_ADMIN")
                        ||role.equals("ROLE_TEACHER")
                        ||role.equals("ROLE_STUDENT")
                        ||role.equals("ROLE_SUPERVISOR")
                )
        ){
            throw new RoleNotValidException("User role must be ROLE_ADMIN, ROLE_TEACHER, ROLE_STUDENT, or ROLE_SUPERVISOR");
        }
        return ResponseUtility.responseTemplateForSingleData(
                "successful",
                userService.listOfUserByRole(role),
                "All users with Role : "+role,
                200
        );
    }

    @GetMapping("/users/role/{role}/paged")
    public ResponseEntity<?> listOfUserByRolePaged(
            @RequestParam(value = "pageNo", defaultValue = "0") int pageNo,
            @RequestParam(value = "size", defaultValue = "100") int size,
            @PathVariable("role") String role
    ){
        if (
                !(
                role.equals("ROLE_ADMIN")
                ||role.equals("ROLE_TEACHER")
                ||role.equals("ROLE_STUDENT")
                ||role.equals("ROLE_SUPERVISOR")
                )
        ){
            throw new RoleNotValidException(
                    "User role must be ROLE_ADMIN, ROLE_TEACHER, ROLE_STUDENT, or ROLE_SUPERVISOR"
            );
        }
        return ResponseUtility.responseTemplateForSingleData(
                "successful",
                userService.listOfUserByRole(
                        role,
                        pageNo,
                        size
                ),
                "all users with the selected role",
                200
        );
    }

    @GetMapping("/users")
    public ResponseEntity<?> getAllUsers(){
        return ResponseUtility.responseTemplateForMultipleData(
                "successful",
                userService.findAllUsers().toArray(),
                "All users ",
                200
        );
    }

    @GetMapping("/users/paged")
    public ResponseEntity<?> getAllUsersPaged(
            @RequestParam(value = "pageNo",defaultValue = "0") int pageNo,
            @RequestParam(value = "size",defaultValue = "100")int size
    ){
        return ResponseEntity.ok(
                Map.of(
                        "status","successful",
                        "data",new PagedModel<>(
                                userService.findAllUsersPage(
                                        pageNo,
                                        size
                                )
                        ),
                        "time",getNowTime()
                )
        );
    }

    @DeleteMapping("/user/email")
     public ResponseEntity<?> deleteUserByEmail(
             @RequestBody DtoForEmailAndAdminPasswordInRequest dto
    ){
        userService.deleteUserByEmail(
                dto.getEmail(),
                dto.getAdminPassword()
        );
        return ResponseEntity.ok(
                Map.of(
                        "status","successful",
                        "message","users with email deleted successfully",
                        "time",getNowTime()
                )
        );
    }

    @DeleteMapping("/user/id")
    public ResponseEntity<?> deleteById(
            @RequestBody DtoForIdAndAdminPasswordInRequest dto
    ){
        userService.deleteUserById(
                dto.getId(),
                dto.getAdminPassword()
        );
        return ResponseEntity.ok(
                Map.of(
                        "status","successful",
                        "message","users with id deleted successfully",
                        "time",getNowTime()
                )
        );
    }

    @DeleteMapping("/users/ids")
    public ResponseEntity<?> deleteUsersInBatchByID(
            @RequestBody DtoForIdsAndPasswordInRequest dto
    ){
        userService.deleteUserInBatchById(
                dto.getIds(),
                dto.getAdminPassword()
        );
        return ResponseEntity.ok(
                Map.of(
                        "status","successful",
                        "message","all users with ids deleted successfully",
                        "time",getNowTime()
                )
        );
    }

    @DeleteMapping("/users/emails")
    public ResponseEntity<?> deleteUsersInBatchByEmail(
            @RequestBody DtoForEmailsAndPasswordInRequest dto
    ){
        userService.deleteUserInBatchEmail(dto.getEmails(),dto.getAdminPassword());
        return ResponseEntity.ok(
                Map.of(
                        "status","successful",
                        "message","all users deleted successfully with emails",
                        "time",getNowTime()
                )
        );
    }

    @PatchMapping("/suspend/user/id")
    public ResponseEntity<?> suspendUserById(
            @RequestBody DtoForIdAndAdminPasswordInRequest dto
    ){
        return ResponseUtility.responseTemplateForSingleData(
                "successful",
                userService.suspendUserById(
                        dto.getId(),
                        dto.getAdminPassword()
                ),
                "User suspended successful",
                200
        );
    }

    @PatchMapping("/unsuspend/user/id")
    public ResponseEntity<?> unsuspendUserById(
            @RequestBody DtoForIdAndAdminPasswordInRequest dto
    ){
        return ResponseUtility.responseTemplateForSingleData(
                "successful",
                userService.unsuspendUserById(
                        dto.getId(),
                        dto.getAdminPassword()
                ),
                "User unsuspended successful",
                200
        );
    }

    @PatchMapping("/suspend/user/email")
    public ResponseEntity<?> suspendUserByEmail(
            @RequestBody DtoForEmailAndAdminPasswordInRequest dto
    ){
        return ResponseUtility.responseTemplateForSingleData(
                "successful",
                userService.suspendUserByEmail(
                        dto.getEmail(),
                        dto.getAdminPassword()
                ),
                "user suspend successfully",
                200
        );
    }

    @PatchMapping("/unsuspend/user/email")
    public ResponseEntity<?> unsuspendUserByEmail(
            @RequestBody DtoForEmailAndAdminPasswordInRequest dto
    ){
        return ResponseEntity.ok(
                Map.of(
                        "status","successful",
                        "data", userService.unsuspendUserByEmail(
                                dto.getEmail(),
                                dto.getAdminPassword()
                        ),
                        "time",getNowTime()
                )
        );
    }

    @PatchMapping("/update/user/password/email")
    public ResponseEntity<?> updatePasswordByEmail(
            @RequestBody DtoForEmailAnd2PasswordsInRequest dto
    ){
        return ResponseUtility.responseTemplateForSingleData(
                "successful",
                userService.updateUserPasswordByEmail(
                        dto.getEmail(),
                        dto.getPassword(),
                        dto.getAdminPassword()
                ),
                "User password successfully",
                200
        );
    }

    @PatchMapping("/update/user/password/id")
    public ResponseEntity<?> updateUserPasswordById(
            @RequestBody DtoForUserIdAndPasswordInRequest dto
    ){
        return ResponseUtility.responseTemplateForSingleData(
                "successful",
                userService.updateUserPasswordById(
                        dto.getId(),
                        dto.getPassword(),
                        dto.getAdminPassword()
                ),
                "User with id has been updated successfully",
                200
        );
    }

    @PatchMapping("/update/user/role/id")
    public ResponseEntity<?> updateUserRoleById(
            @RequestBody DtoForRoleAndIdAndPassworedInRequest dto
    ){
        return ResponseUtility.responseTemplateForSingleData(
                "successful",
                userService.updateUserRoleById(
                        dto.getRole(),
                        dto.getId(),
                        dto.getPassword()
                ),
                "user role updated successfully ",
                200
        );
    }

    @PatchMapping("/update/user/role/email")
    public ResponseEntity<?> updateUserRoleByEmail(
            @RequestBody DtoForRoleAndEmailAndPasswordInRequest dto
    ){
        return ResponseUtility.responseTemplateForSingleData(
                "successful",
                userService.updateUserRoleByEmail(
                        dto.getRole(),
                        dto.getEmail(),
                        dto.getPassword()
                ),
                "users role is updated successfully",
                200
        );
    }

    @PatchMapping("/update/my/email")
    public ResponseEntity<?>updateUserEmail(@RequestBody Map<String,String> request){
        String email= request.get("newEmail");
        return ResponseUtility.responseTemplateForSingleData(
                "successful",
                userService.updateUserEmail(email),
                "your email has been updated",
                200
        );
    }

    @PatchMapping("/update/my/password")
    public ResponseEntity<?> updateUserPassword(@RequestBody Map<String,String> request){
        String password= request.get("password");
        return ResponseUtility.responseTemplateForSingleData(
                "successful",
                userService.updateUserPassword(password),
                "your password has been updated successfully ",
                200
        );
    }

    @GetMapping("/test")
    public ResponseEntity<?> test(){
        return ResponseEntity.ok(
                Map.of("status", "successful")
        );
    }
}
