package com.FinalYearProject.FinalYearProject.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class StudentController {
    @GetMapping("/student-dashboard")
    public String student(){
        return "student";
    }
}
