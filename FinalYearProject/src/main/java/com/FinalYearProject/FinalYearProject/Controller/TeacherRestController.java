package com.FinalYearProject.FinalYearProject.Controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("${app.version}/teacher")
@RestController
public class TeacherRestController {
    @GetMapping("/test")
    public String test(){
        return "hii";
    }
}
