package com.FinalYearProject.FinalYearProject.Controller;


import com.FinalYearProject.FinalYearProject.Domain.User;
import com.FinalYearProject.FinalYearProject.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@Controller
// this class is only used to load front end ie get request
public class PageController {
    @Autowired
    public UserService userService;

    @GetMapping("/api/v1/confirm?token=")
    public ResponseEntity<?> conformation(@RequestParam("token") String token){
    try{
        System.out.println("your account not verified yet before calling userService.verifyToken");
        Boolean ConformToken= userService.verifyToken(token);
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
        return ResponseEntity.badRequest().body("invalid token"+e);
    }
    }
    @PreAuthorize("hasRole('ROLE_STUDENT')")
    @GetMapping("/student-dashboard")
    public String student(){
        return "student-dashboard";
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/admin-dashboard")
    public String admin(){
        return "admin-dashboard";
    }

    @PreAuthorize("hasRole('ROLE_SUPERVISOR')")
    @GetMapping("/supervisor-dashboard")
    public String supervisor(){
    return "supervisor-dashboard";
    }

    @PreAuthorize("hasRole('ROLE_TEACHER')")
    @GetMapping("/teacher-dashboard")
    public String teacher(){
        return "teacher-dashboard";
    }
}