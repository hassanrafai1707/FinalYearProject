package com.FinalYearProject.FinalYearProject.Controller;

import com.FinalYearProject.FinalYearProject.Domain.User;
import com.FinalYearProject.FinalYearProject.Service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RequestMapping("${app.version}/auth")
@RestController
@AllArgsConstructor
//this class is used to handle authorization and validation
public class AuthController {
    @Autowired
    private final UserService userService;

    @PostMapping("/login")
    public ResponseEntity<?> processLogin(@RequestBody User user){
        try {
            String token = userService.verifyLogin(user);
            return ResponseEntity
                    .ok(
                            Map.of(
                                    "token",token,
                                    "role",user.getRole()

                            )
                    );
        }
        catch (Exception e){
            return  ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(
                            Map.of(
                                    "message","Invalid credentials"
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
                            "message",
                            "User registered successfully. Please verifyLogin your email.",
                            "user",
                            saveUser
                    )
            );
        }
        catch (Exception e){
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(
                            Map.of(
                                    "message",
                                    "Registration failed",
                                    "error",
                                    e.getMessage()
                            )
                    );
        }
    }

    @PostMapping("/confirm")
    public ResponseEntity<?> conformation(@RequestParam("token") String token,
                                          @RequestParam("OTP") int Otp){
        try{
            System.out.println("your account not verified yet before calling userService.verifyTokenAndOTP");
            Boolean ConformToken= userService.verifyTokenAndOTP(token,Otp);
            return ResponseEntity.ok(
                    Map.of(
                            "message",
                            "User confirm",
                            "User Confirm token",
                            ConformToken
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
