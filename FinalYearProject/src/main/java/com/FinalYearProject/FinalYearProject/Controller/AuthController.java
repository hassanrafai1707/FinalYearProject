package com.FinalYearProject.FinalYearProject.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


@Controller
public class AuthController {
    @GetMapping("/login")
    public String login(){
        return "login";
    }
    @GetMapping("/register")
    public String register(){
        return "register";
    }
    @GetMapping("/confirm")
    public String conformation(
            @RequestParam("token") String token,
            @RequestParam("email") String email,
            Model model
    ) {
        model.addAttribute("token",token);
        model.addAttribute("email",email);
        return "otp";
    }
}
