package com.FinalYearProject.FinalYearProject.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;


@Controller
//this class will have all path that do not blong anywhere
public class CommonController {
    @GetMapping("/")
    public String home(){
        return "index";
    }
}
