package com.FinalYearProject.FinalYearProject.Controller;

import com.FinalYearProject.FinalYearProject.DTO.DtoForEmaiAndIdInRequest;
import com.FinalYearProject.FinalYearProject.DTO.DtoForEmailAndPasswordInRequest;
import com.FinalYearProject.FinalYearProject.DTO.DtoForOldEmailAndNewEmailInRequest;
import com.FinalYearProject.FinalYearProject.DTO.DtoFortUserIdAndPasswordInRequest;
import com.FinalYearProject.FinalYearProject.Domain.User;
import com.FinalYearProject.FinalYearProject.Service.QuestionService;
import com.FinalYearProject.FinalYearProject.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RequestMapping("${app.version}/student")
@RestController
public class StudentRestController {
    @Autowired
    private UserService userService;
    @Autowired
    private QuestionService questionService;

    @PatchMapping("/updateUserEmailById")
    public ResponseEntity<?> updateUserEmailById(@RequestBody DtoForEmaiAndIdInRequest
                                                         dto){
        Long Id= dto.getId();
        String email= dto.getEmail();
        User upDatedUser= userService.updateUserEmailById(Id,email);
        return ResponseEntity.ok(
                Map.of(
                        "states","successful",
                        "updatedUser",upDatedUser
                )
        );
    }

    @PatchMapping("/updateUserEmailByEmail")
    public ResponseEntity<?> updateUserEmailByEmail(@RequestBody DtoForOldEmailAndNewEmailInRequest
                                                            dto){
        String newEmail= dto.getNewEmail();
        String oldEmail= dto.getOldEmail();
        User updatedUser=userService.updateUserEmailByEmail(oldEmail,newEmail);
        return ResponseEntity.ok(
                Map.of(
                        "states","successful",
                        "updatedUser",updatedUser
                )
        );
    }

    @PatchMapping("/updateUserPasswordByEmail")
    public ResponseEntity<?> updateUserPasswordByEmail(@RequestBody DtoForEmailAndPasswordInRequest
                                                               dto){
        String email= dto.getEmail();
        String newPassword= dto.getPassword();
        User updatedUser =userService.updateUserPasswordByEmail(email,newPassword);
        return ResponseEntity.ok(
                Map.of(
                        "states","successful",
                        "updatedUser",updatedUser
                )
        );
    }

    @PatchMapping("/updateUserPasswordById")
    public ResponseEntity<?> updateUserPasswordById(@RequestBody DtoFortUserIdAndPasswordInRequest
                                                            dto){
        Long Id=dto.getId();
        String password= dto.getPassword();
        User updatedUser = userService.updateUserPasswordById(Id,password);
        return ResponseEntity.ok(
                Map.of(
                        "states","successful",
                        "updatedUser",updatedUser
                )
        );
    }

    @GetMapping("/test")
    public String test(){
        return "hii";
    }
}
