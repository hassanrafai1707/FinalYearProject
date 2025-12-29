package com.FinalYearProject.FinalYearProject.Controller;

import com.FinalYearProject.FinalYearProject.DTO.UserDto.*;
import com.FinalYearProject.FinalYearProject.Exceptions.UserEeceptions.RoleNotValidException;
import com.FinalYearProject.FinalYearProject.Service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.web.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Admin REST Controller for User Management Operations
 * PURPOSE:
 * Provides comprehensive user administration endpoints for system administrators.
 * All endpoints are prefixed with versioned admin path (${app.version}/admin).
 * SECURITY CONTEXT:
 * - Requires ROLE_ADMIN authority (configured in SecurityConfig)
 * - All operations require admin authentication via JWT token
 * - Sensitive operations (deletes, role changes) require additional admin password verification
 * USER MANAGEMENT OPERATIONS:
 * 1. USER RETRIEVAL:
 *    - findUserById: Get user by database ID
 *    - findByEmail: Get user by email address
 *    - listOfUserByRole: Get all users with specific role
 *    - getAllUsers: Get all users in system
 *    - getAllUsersPaged: Get paginated list of users
 * 2. USER DELETION:
 *    - deleteUserByEmail: Delete single user by email (requires admin password)
 *    - deleteUserById: Delete single user by ID (requires admin password)
 *    - deleteUsersInBatchByID: Bulk delete by IDs (requires admin password)
 *    - deleteUsersInBatchByEmail: Bulk delete by emails (requires admin password)
 * 3. USER SUSPENSION/MANAGEMENT:
 *    - suspendUserById/ByEmail: Suspend user account (prevent login)
 *    - unsuspendUserById/ByEmail: Reactivate suspended account
 * 4. USER UPDATE OPERATIONS:
 *    - updateUserPasswordByEmail/ById: Reset user password (requires admin password)
 *    - updateUserRoleByEmail/ById: Change user role/authority (requires admin password)
 *    - updateUserEmail: Change user's email address
 *    - updateUserPassword: Change user's password
 * 5. UTILITY ENDPOINTS:
 *    - logout: Logout endpoint (handled by stateless JWT - typically invalidates token)
 *    - test: Simple endpoint for connectivity testing
 * REQUEST/RESPONSE PATTERN:
 * - Uses DTOs (Data Transfer Objects) for structured request data
 * - Returns consistent JSON response format: {"status": "...", "data": ...}
 * - Error handling via global exception handler (not shown in this controller)
 * SECURITY NOTES:
 * - Admin password verification for destructive operations adds extra security layer
 * - JWT token validation happens before reaching these endpoints
 * - No session management (stateless) - logout typically handled via token blacklisting
 * PERFORMANCE CONSIDERATIONS:
 * - getAllUsersPaged supports pagination for large user databases
 * - Batch operations minimize database round-trips
 * - Consider adding caching for frequently accessed user data
 */
@RequestMapping("${app.version}/admin")
@RestController
public class AdminRestController {
    private final UserService userService;

    AdminRestController(UserService userService){
        this.userService=userService;
    }

    @GetMapping("/user/id/{id}")
    public ResponseEntity<?> findUserById(@PathVariable("id") Long id){
        return ResponseEntity.ok(
                Map.of(
                        "status","successful" ,
                        "data",userService.findUserById(id)
                )
        );
    }

    @GetMapping("/user/email/{email}")
    public ResponseEntity<?> findByEmail(@PathVariable("email") String email) {
        return ResponseEntity.ok(
                Map.of(
                        "status", "successful",
                        "data", userService.findByEmail(email)
                )
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
        return ResponseEntity.ok(
                Map.of(
                        "status","successful",
                        "data",userService.listOfUserByRole(role)
                )
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
        return ResponseEntity.ok(
                Map.of(
                        "status","successful",
                        "data", new PagedModel<>(
                                userService.listOfUserByRole(
                                        role,
                                        pageNo,
                                        size
                                )
                        )
                )
        );
    }

    @GetMapping("/users")
    public ResponseEntity<?> getAllUsers(){
        return ResponseEntity.ok(
                Map.of(
                        "status","successful",
                        "data", userService.findAllUsers()
                )
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
                        )
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
                        "message","users with email deleted successfully"
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
                        "message","users with id deleted successfully"
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
                        "message","all users with ids deleted successfully"
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
                        "message","all users deleted successfully with emails"
                )
        );
    }

    @PatchMapping("/suspend/user/id")
    public ResponseEntity<?> suspendUserById(
            @RequestBody DtoForIdAndAdminPasswordInRequest dto
    ){
        return ResponseEntity.ok(
                Map.of(
                        "status","successful",
                        "data", userService.suspendUserById
                                (
                                        dto.getId(),
                                        dto.getAdminPassword()
                                )
                )
        );
    }

    @PatchMapping("/unsuspend/user/id")
    public ResponseEntity<?> unsuspendUserById(
            @RequestBody DtoForIdAndAdminPasswordInRequest dto
    ){
        return ResponseEntity.ok(
                Map.of(
                        "status","successful",
                        "data", userService.unsuspendUserById(
                                dto.getId(),
                                dto.getAdminPassword()
                        )
                )
        );
    }

    @PatchMapping("/suspend/user/email")
    public ResponseEntity<?> suspendUserByEmail(
            @RequestBody DtoForEmailAndAdminPasswordInRequest dto
    ){
        return ResponseEntity.ok(
                Map.of(
                        "status","successful",
                        "data", userService.suspendUserByEmail(
                                dto.getEmail(),
                                dto.getAdminPassword()
                        )
                )
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
                        )
                )
        );
    }

    @PatchMapping("/update/user/password/email")
    public ResponseEntity<?> updatePasswordByEmail(
            @RequestBody DtoForEmailAnd2PasswordsInRequest dto
    ){
        return ResponseEntity
                .ok(
                        Map.of(
                                "status","successful",
                                "data",userService.updateUserPasswordByEmail(
                                        dto.getEmail(),
                                        dto.getPassword(),
                                        dto.getAdminPassword()
                                )
                        )
                );
    }

    @PatchMapping("/update/user/password/id")
    public ResponseEntity<?> updateUserPasswordById(
            @RequestBody DtoForUserIdAndPasswordInRequest dto
    ){
        return ResponseEntity.ok(
                Map.of(
                        "status","successful",
                        "data",userService.updateUserPasswordById(
                                dto.getId(),
                                dto.getPassword(),
                                dto.getAdminPassword()
                        )
                )
        );
    }

    @PatchMapping("/update/user/role/id")
    public ResponseEntity<?> updateUserRoleById(
            @RequestBody DtoForRoleAndIdAndPassworedInRequest dto
    ){
        return ResponseEntity
                .ok(
                        Map.of(
                                "status","successful",
                                "data", userService.updateUserRoleById(
                                        dto.getRole(),
                                        dto.getId(),
                                        dto.getPassword()
                                )
                        )
                );
    }

    @PatchMapping("/update/user/role/email")
    public ResponseEntity<?> updateUserRoleByEmail(
            @RequestBody DtoForRoleAndEmailAndPasswordInRequest dto
    ){
        return ResponseEntity
                .ok(
                        Map.of(
                                "status","successful",
                                "data",userService.updateUserRoleByEmail(
                                        dto.getEmail(),
                                        dto.getRole(),
                                        dto.getPassword()
                                )
                        )
                );
    }

    @PatchMapping("/update/my/email")
    public ResponseEntity<?>updateUserEmail(@RequestBody Map<String,String> request){
        String email= request.get("newEmail");
        return ResponseEntity.ok(
                Map.of(
                        "status","successful",
                        "data",userService.updateUserEmail(email)
                )
        );
    }

    @PatchMapping("/update/my/password")
    public ResponseEntity<?> updateUserPassword(@RequestBody Map<String,String> request){
        String password= request.get("password");
        return ResponseEntity.ok(
                Map.of(
                        "status","successful",
                        "data",userService.updateUserPassword(password)
                )
        );
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logoutProcesser (HttpServletRequest token){
        return ResponseEntity.ok(
                Map.of(
                        "status","successful",
                        "message","user logout successfully"
                )
        );
    }

    @GetMapping("/test")
    public ResponseEntity<?> test(){
        return ResponseEntity.ok(
                Map.of("status", "successful")
        );
    }
}
