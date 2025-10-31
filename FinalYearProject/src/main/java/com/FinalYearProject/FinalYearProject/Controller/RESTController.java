package com.FinalYearProject.FinalYearProject.Controller;

import com.FinalYearProject.FinalYearProject.Domain.User;
import com.FinalYearProject.FinalYearProject.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1")
public class RESTController {
    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user){
        try{
            User  SaveUser =userService.saveUser(user);
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(
                            Map.of(
                                    "message",
                                    "User registered successfully. Please verify your email.",
                                    "user",
                                    SaveUser
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
    @PostMapping("/login")
    public ResponseEntity<?> ProcessLogin(@RequestBody User user) {
        try {
            String token = userService.verify(user);
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(
                            Map.of(
                            "message",
                            "Successful Login",
                            "token",
                            token
            ));
        }
        catch (Exception e){
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(
                            Map.of(
                                    "message",
                                    "Invalid email or password",
                                    "error",
                                    e.getMessage()
                    )
            );
        }
    }
}
