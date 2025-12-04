package com.FinalYearProject.FinalYearProject.Controller;

import com.FinalYearProject.FinalYearProject.DTO.DtoForEmailAndPasswordInRequest;
import com.FinalYearProject.FinalYearProject.DTO.DtoFortUserIdAndPasswordInRequest;
import com.FinalYearProject.FinalYearProject.DTO.DtoForEmaiAndIdInRequest;
import com.FinalYearProject.FinalYearProject.DTO.DtoForOldEmailAndNewEmailInRequest;
import com.FinalYearProject.FinalYearProject.Domain.User;
import com.FinalYearProject.FinalYearProject.Service.JwtService;
import com.FinalYearProject.FinalYearProject.Service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
@RequestMapping("${app.version}/admin")
@RestController
public class AdminRestController {
    @Autowired
    private UserService userService;
    @Autowired
    private JwtService jwtService;

    @PostMapping("/findUserById")
    public ResponseEntity<?> findUserById(@RequestBody Map<String ,Long> request){
        Long Id= request.get("id");
        User existingUser = userService.getUserById(Id);
        return ResponseEntity.ok(
                Map.of(
                        "status","successful" ,
                        "user",existingUser
                )
        );
    }

    @PostMapping("/findByEmail")
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

    @PostMapping("/listOfUserByRole")
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

    @PostMapping("/getAllUsers")
    public ResponseEntity<?> getAllUsers(){
        List<User> AllUsers=userService.getAllUsers();
        return ResponseEntity.ok(
                Map.of(
                        "status","successful",
                        "allUsers", AllUsers
                )
        );
    }

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

    @PostMapping("/suspendUserById")
    public ResponseEntity<?> suspendUserById(@RequestBody Map<String,Long> request){
        Long Id= request.get("id");
        String Message= userService.suspendUserById(Id);
        return ResponseEntity.ok(
                Map.of(
                        "states","successful",
                        "message", Message
                )
        );
    }

    @PostMapping("/suspendUserByEmail")
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

    @PostMapping("/updateUserEmailById")
    public ResponseEntity<?>updateUserEmailById(@RequestBody DtoForEmaiAndIdInRequest
                                                            dto){
        Long Id= dto.getId();
        String email= dto.getEmail();
        User upDatedUser= userService.updateUserEmailById(Id,email);
        return ResponseEntity.ok(
                Map.of(
                        "states","successful",
                        "updatedUser",upDatedUser
                )
        );
    }

    @PostMapping("/updateUserEmailByEmail")
    public ResponseEntity<?> updateUserEmailByEmail(@RequestBody DtoForOldEmailAndNewEmailInRequest
                                                            dto){
        String newEmail= dto.getNewEmail();
        String oldEmail= dto.getOldEmail();
        User updatedUser=userService.updateUserEmailByEmail(oldEmail,newEmail);
        return ResponseEntity.ok(
                Map.of(
                        "states","successful",
                        "updatedUser",updatedUser
                )
        );
    }

    @PostMapping("/updateUserPasswordByEmail")
    public ResponseEntity<?> updateUserPasswordByEmail(@RequestBody DtoForEmailAndPasswordInRequest
                                                       dto){
        String email= dto.getEmail();
        String newPassword= dto.getPassword();
        User updatedUser =userService.updateUserPasswordByEmail(email,newPassword);
        return ResponseEntity.ok(
                Map.of(
                        "states","successful",
                        "updatedUser",updatedUser
                )
        );
    }

    @PostMapping("/updateUserPasswordById")
    public ResponseEntity<?> updateUserPasswordById(@RequestBody DtoFortUserIdAndPasswordInRequest
                                                    dto){
        Long Id=dto.getId();
        String password= dto.getPassword();
        User updatedUser = userService.updateUserPasswordById(Id,password);
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
