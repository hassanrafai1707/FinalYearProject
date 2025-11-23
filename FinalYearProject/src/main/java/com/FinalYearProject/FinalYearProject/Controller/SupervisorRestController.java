package com.FinalYearProject.FinalYearProject.Controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("${app.version}/supervisor")
@RestController
public class SupervisorRestController {
    @GetMapping("/test")
    public String test(){
        return "hii";
    }

}
