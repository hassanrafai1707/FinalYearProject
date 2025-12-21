package com.FinalYearProject.FinalYearProject.Controller;

import com.FinalYearProject.FinalYearProject.DTO.UserDto.*;
import com.FinalYearProject.FinalYearProject.Domain.User;
import com.FinalYearProject.FinalYearProject.Service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.web.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
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
    @Autowired
    private UserService userService;

    @GetMapping("/findUserById")
    public ResponseEntity<?> findUserById(@RequestBody Map<String ,Long> request){
        Long Id= request.get("id");
        User existingUser = userService.findUserById(Id);
        return ResponseEntity.ok(
                Map.of(
                        "status","successful" ,
                        "user",existingUser
                )
        );
    }

    @GetMapping("/findByEmail")
    public ResponseEntity<?> findByEmail(@RequestBody Map<String , String> requst) {
        String Email = requst.get("email");
        User existingUser = userService.findByEmail(Email);
        return ResponseEntity.ok(
                Map.of(
                        "status", "successful",
                        "user", existingUser
                )
        );
    }

    @GetMapping("/listOfUserByRole")
    public ResponseEntity<?> listOfUserByRole(@RequestBody Map<String ,String > request){
        String Role=request.get("role");
        List<User> userByRole =userService.listOfUserByRole(Role);
        return ResponseEntity.ok(
                Map.of(
                        "status","successful",
                        "listOfUsersWithTheGivenRole",userByRole
                )
        );
    }

    @GetMapping("/listOfUserByRolePaged")
    public PagedModel<User> listOfUserByRolePaged(
            @RequestParam(value = "pageNo", defaultValue = "0") int pageNo,
            @RequestParam(value = "size", defaultValue = "100") int size,
            @RequestBody Map<String,String> request
    ){
        return new PagedModel<>(userService.listOfUserByRole(request.get("role"),pageNo,size));
    }

    @GetMapping("/getAllUsers")
    public ResponseEntity<?> getAllUsers(){
        List<User> AllUsers=userService.findAllUsers();
        return ResponseEntity.ok(
                Map.of(
                        "status","successful",
                        "allUsers", AllUsers
                )
        );
    }

    @GetMapping("/getAllUsersPaged")
    public PagedModel<User> getAllUsersPaged(
            @RequestParam(value = "pageNo",defaultValue = "0") int pageNo,
            @RequestParam(value = "size",defaultValue = "100")int size
    ){
        return new PagedModel<>(userService.findAllUsersPage(pageNo, size));
    }

    @DeleteMapping("/deleteUserByEmail")
     public ResponseEntity<?> deleteUserByEmail(@RequestBody DtoForEmailAndAdminPasswordInRequest dto){
        userService.deleteUserByEmail(dto.getEmail(),dto.getAdminPassword());
        return ResponseEntity.ok(
                Map.of(
                        "status","successful",
                        "message","users with email deleted successfully"
                )
        );
    }

    @DeleteMapping("/deleteUserById")
    public ResponseEntity<?> deleteById( @RequestBody DtoForIdAndAdminPasswordInRequest dto){
        userService.deleteUserById(dto.getId(),dto.getAdminPassword());
        return ResponseEntity.ok(
                Map.of(
                        "states","successful",
                        "message","users with id deleted successfully"
                )
        );
    }

    @DeleteMapping("/deleteUsersInBatchByID")
    public ResponseEntity<?> deleteUsersInBatchByID(@RequestBody DtoForIdsAndPasswordInRequest dto){
        userService.deleteUserInBatchById(dto.getIds(),dto.getAdminPassword());
        return ResponseEntity.ok(
                Map.of(
                        "status","successful",
                        "message","all users with ids deleted successfully"
                )
        );
    }

    @DeleteMapping("/deleteUsersInBatchByEmail")
    public ResponseEntity<?> deleteUsersInBatchByEmail(@RequestBody DtoForEmailsAndPasswordInRequest dto){
        userService.deleteUserInBatchEmail(dto.getEmails(),dto.getAdminPassword());
        return ResponseEntity.ok(
                Map.of(
                        "status","successful",
                        "message","all users deleted successfully with emails"
                )
        );
    }

    @PatchMapping("/suspendUserById")
    public ResponseEntity<?> suspendUserById(@RequestBody DtoForIdAndAdminPasswordInRequest dto){
        User user = userService.suspendUserById(dto.getId(),dto.getAdminPassword());
        return ResponseEntity.ok(
                Map.of(
                        "states","successful",
                        "updated user", user
                )
        );
    }

    @PatchMapping("/unsuspendUserById")
    public ResponseEntity<?> unsuspendUserById(@RequestBody DtoForIdAndAdminPasswordInRequest dto){
        User user =userService.unsuspendUserById(dto.getId(),dto.getAdminPassword());
        return ResponseEntity.ok(
                Map.of(
                        "states","successful",
                        "message", user
                )
        );
    }

    @PatchMapping("/suspendUserByEmail")
    public ResponseEntity<?> suspendUserByEmail(@RequestBody DtoForEmailAndAdminPasswordInRequest dto){
        User user= userService.suspendUserByEmail(dto.getEmail(),dto.getAdminPassword());
        return ResponseEntity.ok(
                Map.of(
                        "states","successful",
                        "message", user
                )
        );
    }

    @PatchMapping("/unsuspendUserByEmail")
    public ResponseEntity<?> unsuspendUserByEmail(@RequestBody DtoForEmailAndAdminPasswordInRequest dto){
        User user =userService.unsuspendUserByEmail(dto.getEmail(),dto.getAdminPassword());
        return ResponseEntity.ok(
                Map.of(
                        "states","successful",
                        "message", user
                )
        );
    }

    @PatchMapping("/updateUserPasswordByEmail")
    public ResponseEntity<?> updatePasswordByEmail(@RequestBody DtoForEmailAnd2PasswordsInRequest dto){
        User user=userService.updateUserPasswordByEmail(
                dto.getEmail(),
                dto.getPassword(),
                dto.getAdminPassword()
        );
        return ResponseEntity
                .ok(
                        Map.of(
                                "states","successful",
                                "updated user",user
                        )
                );
    }

    @PatchMapping("/updateUserPasswordById")
    public ResponseEntity<?> updateUserPasswordById(@RequestBody DtoForUserIdAndPasswordInRequest dto){
        User user=userService.updateUserPasswordById(
                dto.getId(),
                dto.getPassword(),
                dto.getAdminPassword()
        );
        return ResponseEntity.ok(
                Map.of(
                        "states","successful",
                        "updated user",user
                )
        );
    }

    @PatchMapping("/updateUserRoleById")
    public ResponseEntity<?> updateUserRoleById(@RequestBody DtoForRoleAndIdAndPassworedInRequest dto){
        return ResponseEntity
                .ok(
                        Map.of(
                                "status","successful",
                                "updated user", userService.updateUserRoleById(
                                        dto.getRole(),
                                        dto.getId(),
                                        dto.getPassword()
                                )
                        )
                );
    }

    @PatchMapping("/updateUserRoleByEmail")
    public ResponseEntity<?> updateUserRoleByEmail(@RequestBody DtoForRoleAndEmailAndPasswordInRequest dto){
        return ResponseEntity
                .ok(
                        Map.of(
                                "status","successful",
                                "updated user",userService.updateUserRoleByEmail(
                                        dto.getEmail(),
                                        dto.getRole(),
                                        dto.getPassword()
                                )
                        )
                );
    }

    @PatchMapping("/updateUserEmail")
    public ResponseEntity<?>updateUserEmailById(@RequestBody Map<String,String> request
                                                            ){
        String email= request.get("email");
        User upDatedUser= userService.updateUserEmail(email);
        return ResponseEntity.ok(
                Map.of(
                        "states","successful",
                        "updatedUser",upDatedUser
                )
        );
    }

    @PatchMapping("/updateUserPassword")
    public ResponseEntity<?> updateUserPasswordById(@RequestBody Map<String,String> request){
        String password= request.get("password");
        User updatedUser = userService.updateUserPassword(password);
        return ResponseEntity.ok(
                Map.of(
                        "states","successful",
                        "updatedUser",updatedUser
                )
        );
    }
    @PostMapping("/logout")
    public ResponseEntity<?> logoutProcesser (HttpServletRequest token){
        return ResponseEntity.ok(
                Map.of(
                        "states","successful",
                        "message","user logout successfully"
                )
        );
    }

    @GetMapping("/test")
    public String test(){
        return "hii";
    }
}
