package com.FinalYearProject.FinalYearProject.Controller;

import com.FinalYearProject.FinalYearProject.DTO.UserDto.DtoForEmailAndPasswordInRequest;
import com.FinalYearProject.FinalYearProject.Domain.User;
import com.FinalYearProject.FinalYearProject.Service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RequestMapping("${app.version}/auth")
@RestController
@AllArgsConstructor
//this class is used to handle authorization and validation
public class AuthRestController {
    @Autowired
    private final UserService userService;

    @PostMapping("/login")
    public ResponseEntity<?> processLoginByEmail(@RequestBody DtoForEmailAndPasswordInRequest dto){
        Map<String,Object> temp=userService.verifyLoginByEmail(dto.getEmail(), dto.getPassword());
        User user=(User) temp.get("user");
        return ResponseEntity
                .ok(
                        Map.of(
                                "status","successful",
                                "token",temp.get("token"),
                                "role",user.getRole()
                        )
                );
    }

    @PostMapping("/register")
    public ResponseEntity<?> processRegister (@RequestBody User user){
        User saveUser =userService.creatUser(user);
        return ResponseEntity.ok(
                Map.of(
                        "message", "User registered successfully. Please verifyLoginByEmail your email.",
                        "user", saveUser
                )
        );
    }

    @PostMapping("/confirm")
    public ResponseEntity<?> conformation(@RequestParam("token") String token,
                                          @RequestParam("email") String email,
                                          @RequestBody Map<String,Integer> request){
        int otp=request.get("otp");
        try{
            System.out.println("your account not verified yet before calling userService.verifyTokenAndOTP");
            Boolean ConformToken= userService.verifyTokenAndOTP(email,token,otp);
            return ResponseEntity.ok(
                    Map.of(
                            "message", "User confirm",
                            "User Confirm token", ConformToken
                    )
            );
        }
        catch (Exception e){
            return ResponseEntity.badRequest().body(
                    Map.of(
                            "message",  "Invalid token",
                            "error" ,e.getMessage()
                    )
            );
        }
    }
}
