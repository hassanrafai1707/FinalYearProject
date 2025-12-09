package com.FinalYearProject.FinalYearProject.Controller;

import com.FinalYearProject.FinalYearProject.DTO.UserDto.DtoForEmailAndPasswordInRequest;
import com.FinalYearProject.FinalYearProject.DTO.UserDto.DtoFortUserIdAndPasswordInRequest;
import com.FinalYearProject.FinalYearProject.DTO.UserDto.DtoForEmaiAndIdInRequest;
import com.FinalYearProject.FinalYearProject.DTO.UserDto.DtoForOldEmailAndNewEmailInRequest;
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
