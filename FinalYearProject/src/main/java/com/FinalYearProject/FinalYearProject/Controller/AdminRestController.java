package com.FinalYearProject.FinalYearProject.Controller;

import com.FinalYearProject.FinalYearProject.DTO.UserDto.*;
import com.FinalYearProject.FinalYearProject.Domain.User;
import com.FinalYearProject.FinalYearProject.Service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
@RequestMapping("${app.version}/admin")
@RestController
//TODO for all admin functions ask for password
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
    public Page<User> getAllUsersPaged(
            @RequestParam(value = "pageNo",defaultValue = "0") int pageNo,
            @RequestParam(value = "size",defaultValue = "100")int size
    ){
        return userService.findAllUsersPage(pageNo, size);
    }

    //todo change
    @DeleteMapping("/deleteUserByEmail")
     public ResponseEntity<?> deleteUserByEmail(@RequestBody Map<String,String> request){
        String email=request.get("email");
        String Message=userService.deleteUserByEmail(email);
        return ResponseEntity.ok(
                Map.of(
                        "status","successful",
                        "message",Message
                )
        );
    }

    //todo change
    @DeleteMapping("/deleteUserById")
    public ResponseEntity<?> deleteById( @RequestBody Map<String,Long> request){
        Long Id= request.get("id");
        String message=userService.deleteUserById(Id);
        return ResponseEntity.ok(
                Map.of(
                        "states","successful",
                        "message",message
                )
        );
    }

    //todo change
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

    //todo change
    @PatchMapping("/suspendUserById")
    public ResponseEntity<?> suspendUserById(@RequestBody Map<String,Long> request){
        Long Id= request.get("id");
        String message = userService.suspendUserById(Id);
        return ResponseEntity.ok(
                Map.of(
                        "states","successful",
                        "message", message
                )
        );
    }

    //todo change
    @PatchMapping("/unsuspendUserById")
    public ResponseEntity<?> unsuspendUserById(@RequestBody Map<String,Long> request){
        Long Id= request.get("id");
        String message=userService.unsuspendUserById(Id);
        return ResponseEntity.ok(
                Map.of(
                        "states","successful",
                        "message", message
                )
        );
    }

    //todo change
    @PatchMapping("/suspendUserByEmail")
    public ResponseEntity<?> suspendUserByEmail(@RequestBody Map<String,String> request){
        String email=request.get("email");
        String message = userService.suspendUserByEmail(email);
        return ResponseEntity.ok(
                Map.of(
                        "states","successful",
                        "message", message
                )
        );
    }

    //todo change
    @PatchMapping("/unsuspendUserByEmail")
    public ResponseEntity<?> unsuspendUserByEmail(@RequestBody Map<String,String> request){
        String email=request.get("email");
        String message=userService.unsuspendUserByEmail(email);
        return ResponseEntity.ok(
                Map.of(
                        "states","successful",
                        "message", message
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
