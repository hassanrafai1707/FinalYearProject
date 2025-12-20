package com.FinalYearProject.FinalYearProject.Service;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;
import org.springframework.mail.javamail.JavaMailSender;

@Service
@RequiredArgsConstructor
@AllArgsConstructor
// this class is used to send Email
// using dynamic ip
/**
 * ConformationService - Email Notification Service for Account Verification
 * PURPOSE: Service for sending email notifications, primarily for user account verification during registration. Handles email composition and delivery via Spring Mail.
 * EMAIL FUNCTIONALITY: Sends verification emails with confirmation link and OTP for two-factor email verification. Uses JavaMailSender for SMTP email delivery.
 * VERIFICATION LINK: Constructs URL with token and email parameters for confirm endpoint. Currently uses localhost:8080 - should be configurable for production.
 * TWO-FACTOR VERIFICATION: Includes both token (in URL) and OTP (in email body) for enhanced security. Users must provide both for account activation.
 * EMAIL CONTENT: Personalized with user name. Clear instructions for verification. Includes OTP for alternative verification method.
 * ERROR HANDLING: Try-catch with error logging. Continues execution even if email fails (user can request resend). System.out for debugging in development.
 * CONFIGURATION NOTES: Currently hardcoded to localhost. Should use @Value for configurable base URL (public IP, domain name). Port should match server configuration.
 * SECURITY CONSIDERATIONS: Email contains verification token - ensure HTTPS in production. OTP provides alternative verification if link doesn't work.
 * INTEGRATION: Used by UserService during user registration. Triggered when new user registers or requests email resend.
 * EXTENSION POINTS: Could add email templates, HTML emails, attachment support, queue for bulk sending, and delivery status tracking.
 */
public class ConformationService {
    @Autowired
    public JavaMailSender javaMailSender;
//    @Value("${dynamic.public.ip}")
//    private String PublicIp;
//    @Value("${dynamic.local.ip}")
//    private String LocalIP;

    public void sendEmail(String toEmail, String name , String token,int Otp){
        try{
            //example http://localhost:8080/api/v1/auth/confirm?token=00847b74-7713-4a41-8fea-7d22e1734a7a&email=hassanrafai@gmail.com
        String verificationLink ="http://localhost:8080/api/v1/auth/confirm?token="+token+"&email="+toEmail;//need to spiffy port I am also passing the otp for essay login
            SimpleMailMessage message=new SimpleMailMessage();
             message.setTo(toEmail);
            message.setSubject("Verify Account\n");
            message.setText("Hello "+name+",\n\nPlease verifyLogin your email "+verificationLink+"\n OTP ="+Otp);
            javaMailSender.send(message);
            System.out.println("✅ Email sent successfully to: " + toEmail);
        }
        catch (Exception e){
            System.err.println(e);
        }
    }
}
