package com.FinalYearProject.FinalYearProject.Controller;

import com.FinalYearProject.FinalYearProject.Domain.User;
import com.FinalYearProject.FinalYearProject.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
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
    public ResponseEntity<User> register(@RequestBody User user){
        return ResponseEntity.ok(userService.saveUser(user));
    }
    @PostMapping("/login")
    public ResponseEntity<?> ProcessLogin(@RequestBody User user) {
        try {
            String token = userService.verify(user);
            return ResponseEntity.ok(Map.of(
                    "message",
                    "Successful Login",
                    "token",
                    token
            ));
        }
        catch (Exception e){
            return ResponseEntity.ok(Map.of(
                    "message",
                    "Invalid email or password",
                    "error",
                    e.getMessage()
            ));
        }
    }
}
