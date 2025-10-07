package com.FinalYearProject.FinalYearProject.Controller;

import com.FinalYearProject.FinalYearProject.Domain.User;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@AllArgsConstructor
@NoArgsConstructor
public class PageController {
    @Autowired
    public User user;

    @GetMapping("/")
    public String home(){
        return "index";
    }
    @GetMapping("/login")
    public String login(){
        return "login";
    }
    @GetMapping("/student-dashboard")
    public String student(){
        return "student-dashboard";
    }
    @GetMapping("/admin-dashboard")
    public String admin(){
        return "admin-dashboard";
    }
    @GetMapping("/supervisor-dashboard")
    public String supervisor(){
    return "supervisor-dashboard";
    }
    @GetMapping("teacher-dashboard")
    public String teacher(){
        return "teacher-dashboard";
    }
}