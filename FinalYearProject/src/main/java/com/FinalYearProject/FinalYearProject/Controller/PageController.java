package com.FinalYearProject.FinalYearProject.Controller;


import com.FinalYearProject.FinalYearProject.Domain.User;
import com.FinalYearProject.FinalYearProject.Repository.UserRepository;
import com.FinalYearProject.FinalYearProject.Service.UserService;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Controller
public class PageController {
    @Autowired
    public UserService userService;

    @GetMapping("/")
    public String home(){
        return "index";
    }

    @GetMapping("/login")
    public String login(){
        return "login";
    }

    @PreAuthorize("hasRole('STUDENT')")
    @GetMapping("/student-dashboard")
    public String student(){
        return "student-dashboard";
    }

    @PreAuthorize("hasRole('Admin')")
    @GetMapping("/admin-dashboard")
    public String admin(){
        return "admin-dashboard";
    }

    @PreAuthorize("hasRole('Supervisor')")
    @GetMapping("/supervisor-dashboard")
    public String supervisor(){
    return "supervisor-dashboard";
    }

    @PreAuthorize("hasRole('Teacher')")
    @GetMapping("/teacher-dashboard")
    public String teacher(){
        return "teacher-dashboard";
    }
}