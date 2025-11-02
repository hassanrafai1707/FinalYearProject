package com.FinalYearProject.FinalYearProject.Controller;


import com.FinalYearProject.FinalYearProject.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/api/v1")
@Controller
// this class is only used to load front end ie get request
public class PageController {
    @Autowired
    public UserService userService;


    @GetMapping("/login")
    public String login(){
        return "login";
    }

    @GetMapping("/register")
    public String register(){
        return "register";
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