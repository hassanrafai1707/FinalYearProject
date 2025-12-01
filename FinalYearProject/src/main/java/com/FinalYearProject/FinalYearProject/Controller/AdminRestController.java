package com.FinalYearProject.FinalYearProject.Controller;

import com.FinalYearProject.FinalYearProject.DTO.DtoForAnyRequestThatUserEmailAndPasswordInRequest;
import com.FinalYearProject.FinalYearProject.DTO.DtoForAnyRequestThatUserIdAndPasswordInRequest;
import com.FinalYearProject.FinalYearProject.DTO.DtoForAnyRequestThatUsesEmaiAndIdInRequest;
import com.FinalYearProject.FinalYearProject.DTO.DtoForAnyRequestThatUserOldEmailAndNewEmailInRequest;
import com.FinalYearProject.FinalYearProject.Domain.User;
import com.FinalYearProject.FinalYearProject.Service.JwtService;
import com.FinalYearProject.FinalYearProject.Service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
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

    private ResponseEntity<?> handleUnknownException(Exception e){
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(
                        Map.of(
                                "states","unsuccessful",
                                "message","something went wrong try again",
                                "error", e.getMessage()
                        )
                );
    }

    private ResponseEntity<?> handleUserNotFoudException(UsernameNotFoundException e){
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(
                        Map.of(
                                "states","unsuccessful",
                                "error", e.getMessage()
                        )
                );
    }

    @PostMapping("/findUserById")
    public ResponseEntity<?> findUserById(@RequestBody Map<String ,Long> request){
        Long Id= request.get("id");
        try{
            User existingUser = userService.getUserById(Id);
            return ResponseEntity.ok(
                    Map.of(
                            "status","successful" ,
                            "user",existingUser
                    )
            );
        }
        catch (UsernameNotFoundException e){
            return handleUserNotFoudException(e);
        }
        catch (Exception e){
            return handleUnknownException(e);
        }
    }

    @PostMapping("/findByEmail")
    public ResponseEntity<?> findByEmail(@RequestBody Map<String , String> requst){
       String Email= requst.get("email");
       try{
           User existingUser=userService.findByEmail(Email);
           return ResponseEntity.ok(
                   Map.of(
                           "status","successful" ,
                           "user",existingUser
                   )
           );
       }
       catch (UsernameNotFoundException e){
           return handleUserNotFoudException(e);
       }
       catch (Exception e){
           return handleUnknownException(e);
       }
    }

    @PostMapping("/listOfUserByRole")
    public ResponseEntity<?> listOfUserByRole(@RequestBody Map<String ,String > request){
        String Role=request.get("role");
        try{
            List<User> userByRole =userService.listOfUserByRole(Role);
            return ResponseEntity.ok(
                    Map.of(
                            "status","successful",
                            "listOfUsersWithTheGivenRole",userByRole
                    )
            );
        }
        catch (Exception e){
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(
                            Map.of(
                                    "status","unsuccessful",
                                    "message","check role spelling ",
                                    "error", e.getMessage()
                            )
                    );
        }
    }

    @PostMapping("/getAllUsers")
    public ResponseEntity<?> getAllUsers(){
        try{
            List<User> AllUsers=userService.getAllUsers();
            return ResponseEntity.ok(
                    Map.of(
                            "status","successful",
                            "allUsers", AllUsers
                    )
            );
        }
        catch (Exception e){
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(
                            Map.of(
                                    "message","no users are saves in Db",
                                    "error",e.getMessage()
                            )
                    );
        }
    }

    @DeleteMapping("/deleteUserByEmail")
     public ResponseEntity<?> deleteUserByEmail(@RequestBody Map<String,String> request){
        String email=request.get("email");
        try {
            String Message=userService.deleteUserByEmail(email);
            return ResponseEntity.ok(
                    Map.of(
                            "status","successful",
                            "message",Message
                    )
            );
        }
        catch (UsernameNotFoundException e) {
            return handleUserNotFoudException(e);

        }
        catch (Exception e){
            return handleUnknownException(e);
        }
    }

    @DeleteMapping("/deleteUserById")
    public ResponseEntity<?> deleteById( @RequestBody Map<String,Long> request){
        Long Id= request.get("id");
        try{
            String message=userService.deleteUserById(Id);
            return ResponseEntity.ok(
                    Map.of(
                            "states","successful",
                            "message",message
                    )
            );
        }
        catch (UsernameNotFoundException e) {
           return handleUserNotFoudException(e);
        }
        catch (Exception e){
            return handleUnknownException(e);
        }
    }

    @PostMapping("/suspendUserById")
    public ResponseEntity<?> suspendUserById(@RequestBody Map<String,Long> request){
        Long Id= request.get("id");
        try {
            String Message= userService.suspendUserById(Id);
            return ResponseEntity.ok(
                    Map.of(
                            "states","successful",
                            "message", Message
                    )
            );
        }
        catch (UsernameNotFoundException e) {
            return handleUserNotFoudException(e);
        }
        catch (Exception e){
            return handleUnknownException(e);
        }
    }

    @PostMapping("/suspendUserByEmail")
    public ResponseEntity<?> suspendUserByEmail(@RequestBody Map<String,String> request){
        String email=request.get("email");
        try {
            String message = userService.suspendUserByEmail(email);
            return ResponseEntity.ok(
                    Map.of(
                            "states","successful",
                            "message", message
                    )
            );
        }
        catch (UsernameNotFoundException e){
            return handleUserNotFoudException(e);
        }
        catch (Exception e){
            return handleUnknownException(e);
        }
    }

    @PostMapping("/updateUserEmailById")
    public ResponseEntity<?>updateUserEmailById(@RequestBody DtoForAnyRequestThatUsesEmaiAndIdInRequest
                                                            dto){
        Long Id= dto.getId();
        String email= dto.getEmail();
        try {
            User upDatedUser= userService.updateUserEmailById(Id,email);
            return ResponseEntity.ok(
                    Map.of(
                           "states","successful",
                            "updatedUser",upDatedUser
                    )
            );
        }
        catch (UsernameNotFoundException e){
            return handleUserNotFoudException(e);
        } catch (Exception e) {
            return handleUnknownException(e);
        }
    }

    @PostMapping("/updateUserEmailByEmail")
    public ResponseEntity<?> updateUserEmailByEmail(@RequestBody DtoForAnyRequestThatUserOldEmailAndNewEmailInRequest
                                                            dto){
        String newEmail= dto.getNewEmail();
        String oldEmail= dto.getOldEmail();
        try {
            User updatedUser=userService.updateUserEmailByEmail(oldEmail,newEmail);
            return ResponseEntity.ok(
                    Map.of(
                            "states","successful",
                            "updatedUser",updatedUser
                    )
            );
        }
        catch (UsernameNotFoundException e){
            return handleUserNotFoudException(e);
        }
        catch (Exception e){
            return handleUnknownException(e);
        }
    }

    @PostMapping("/updateUserPasswordByEmail")
    public ResponseEntity<?> updateUserPasswordByEmail(@RequestBody DtoForAnyRequestThatUserEmailAndPasswordInRequest
                                                       dto){
        String email= dto.getEmail();
        String newPassword= dto.getPassword();
        try {
            User updatedUser =userService.updateUserPasswordByEmail(email,newPassword);
            return ResponseEntity.ok(
                    Map.of(
                            "states","successful",
                            "updatedUser",updatedUser
                    )
            );
        }
        catch (UsernameNotFoundException e){
            return handleUserNotFoudException(e);
        }
        catch (Exception e) {
            return handleUnknownException(e);
        }
    }

    @PostMapping("/updateUserPasswordById")
    public ResponseEntity<?> updateUserPasswordById(@RequestBody DtoForAnyRequestThatUserIdAndPasswordInRequest
                                                    dto){
        Long Id=dto.getId();
        String password= dto.getPassword();
        try {
            User updatedUser = userService.updateUserPasswordById(Id,password);
            return ResponseEntity.ok(
                    Map.of(
                            "states","successful",
                            "updatedUser",updatedUser
                    )
            );
        }
        catch (UsernameNotFoundException e){
            return handleUserNotFoudException(e);
        }
        catch (Exception e){
            return handleUnknownException(e);
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logoutProcesser (HttpServletRequest token){
        try {
            return ResponseEntity.ok(
                    Map.of(
                            "states","successful",
                            "message","user logout successfully"
                    )
            );
        }
        catch (Exception e) {
           return handleUnknownException(e);
        }
    }

    @GetMapping("/test")
    public String test(){
        return "hii";
    }
}
