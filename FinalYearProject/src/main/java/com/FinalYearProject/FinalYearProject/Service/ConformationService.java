package com.FinalYearProject.FinalYearProject.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;
import org.springframework.mail.javamail.JavaMailSender;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConformationService {
    @Autowired
    public JavaMailSender javaMailSender;

    public void sendEmail(String toEmail, String name , String token){
        try{
        String verificationLink ="https://Localhost:8080/api/v1/user/confirm?token="+token;
            SimpleMailMessage message=new SimpleMailMessage();
            message.setTo(toEmail);
            message.setSubject("Verify Account");
            message.setText("Hello"+name+",\n\nPlease verify your email "+verificationLink);
            javaMailSender.send(message);
        }
        catch (Exception e){
            System.out.println(e);
        }
    }
}
