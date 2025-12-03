package com.FinalYearProject.FinalYearProject.Controller;

import com.FinalYearProject.FinalYearProject.DTO.DtoForAnyRequestThatUserEmailAndPasswordInRequest;
import com.FinalYearProject.FinalYearProject.Domain.User;
import com.FinalYearProject.FinalYearProject.Service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RequestMapping("${app.version}/auth")
@RestController
@AllArgsConstructor
//this class is used to handle authorization and validation
public class AuthRestController {
    @Autowired
    private final UserService userService;

    @PostMapping("/login")
    public ResponseEntity<?> processLoginByEmail(@RequestBody DtoForAnyRequestThatUserEmailAndPasswordInRequest dto){
        try {
            Map<String,Object> temp=userService.verifyLoginByEmail(dto.getEmail(), dto.getPassword());
            User user=(User) temp.get("user");
            return ResponseEntity
                    .ok(
                            Map.of(
                                    "status","successful",
                                    "token",temp.get("token"),
                                    "role",user.getRole()
                            )
                    );
        }
        catch (UsernameNotFoundException e){
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(
                            Map.of(
                                    "status","unsuccessful",
                                    "error",e.getMessage()
                            )
                    );
        }
        catch (Exception e){
            return  ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(
                            Map.of(
                                    "status","unsuccessful",
                                    "message","Invalid credentials",
                                    "error",e.getMessage()
                            )
                    );
        }
    }
    @PostMapping("/register")
    public ResponseEntity<?> processRegister (@RequestBody User user){
        try {
            User saveUser =userService.saveUser(user);
            return ResponseEntity.ok(
                    Map.of(
                            "message", "User registered successfully. Please verifyLoginByEmail your email.",
                            "user", saveUser
                    )
            );
        }
        catch (Exception e){
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(
                            Map.of(
                                    "message", "Registration failed",
                                    "error", e.getMessage()
                            )
                    );
        }
    }

    @PostMapping("/confirm")
    public ResponseEntity<?> conformation(@RequestParam("token") String token,
                                          @RequestParam("email") String email,
                                          @RequestBody Map<String,Integer> request){
        int otp=request.get("otp");
        try{
            System.out.println("your account not verified yet before calling userService.verifyTokenAndOTP");
            Boolean ConformToken= userService.verifyTokenAndOTP(email,token,otp);
            return ResponseEntity.ok(
                    Map.of(
                            "message", "User confirm",
                            "User Confirm token", ConformToken
                    )
            );
        }
        catch (Exception e){
            return ResponseEntity.badRequest().body(
                    Map.of(
                            "message",  "Invalid token",
                            "error" ,e.getMessage()
                    )
            );
        }
    }
}
