package com.FinalYearProject.FinalYearProject.Controller;

import com.FinalYearProject.FinalYearProject.DTO.UserDto.DtoForEmailAnd2PasswordsInRequest;
import com.FinalYearProject.FinalYearProject.Domain.User;
import com.FinalYearProject.FinalYearProject.Service.JwtService;
import com.FinalYearProject.FinalYearProject.Service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Authentication REST Controller for User Registration and Login
 * PURPOSE: Handles core authentication flows - user registration, email/password login, and email verification. All endpoints are publicly accessible (no authentication required).
 * AUTHENTICATION FLOWS:
 * 1. REGISTRATION FLOW: POST /register creates new user account with pending email verification. Returns success message and user object (without sensitive data). User receives verification email with OTP/token.
 * 2. EMAIL VERIFICATION FLOW: POST /confirm verifies user's email using token and OTP. Parameters: token (email verification token), email, otp (one-time password). Validates both token and OTP for enhanced security. Activates user account upon successful verification.
 * 3. LOGIN FLOW: POST /login authenticates user using email and password. Returns JWT token for authenticated sessions and user's role. Validates credentials and account status (must be verified and not suspended).
 * SECURITY FEATURES: Email verification required before account activation. Two-factor verification (token + OTP) for email confirmation. Password hashing/validation via BCrypt (handled in UserService). JWT token generation upon successful login. Protection against brute force (handled at service layer).
 * REQUEST/RESPONSE PATTERNS:
 * - Login: Returns {"status": "successful", "token": "...", "role": "..."}
 * - Register: Returns {"message": "...", "user": {...}} (user details without password)
 * - Confirm: Returns {"message": "...", "User Confirm token": true/false} or error
 * ERROR HANDLING: Invalid credentials return appropriate HTTP status codes. Unverified accounts prevent login until email verification. Invalid tokens/OTP return 400 Bad Request with error details. Suspended accounts prevent login with appropriate message.
 * INTEGRATION NOTES: Works with email service for verification emails. Integrates with JWT filter for token-based authentication. Supports role-based access after login. Stateless design - no server-side sessions.
 * SECURITY CONSIDERATIONS: Rate limiting recommended for /login and /confirm endpoints. Email verification prevents fake account creation. Token expiration for verification links. Password strength validation at registration. Logging of authentication attempts for audit trail.
 */
@RequestMapping("${app.version}/auth")
@RestController
@AllArgsConstructor
public class AuthRestController {
    @Autowired
    private final UserService userService;
    @Autowired
    private final JwtService jwtService;
//TODO clean from System.out.println in prod
    @PostMapping("/login")
    public ResponseEntity<?> processLoginByEmail(@RequestBody DtoForEmailAnd2PasswordsInRequest dto){
        String temp=userService.verifyLoginByEmail(dto.getEmail(), dto.getPassword());
        return ResponseEntity
                .ok(
                        Map.of(
                                "status","successful",
                                "token",temp,
                                "role",jwtService.extractUserRole(temp)
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
                                          @RequestBody Map<String,Integer> request
    ){
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
