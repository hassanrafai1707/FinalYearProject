package com.FinalYearProject.FinalYearProject.Service;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;
import org.springframework.mail.javamail.JavaMailSender;

@Service
@RequiredArgsConstructor
@Slf4j
@AllArgsConstructor
// this class is used to send Email
// using dynamic ip
public class ConformationService {
    @Autowired
    public JavaMailSender javaMailSender;
    @Value("${dynamic.public.ip}")
    private String PublicIp;
    @Value("${dynamic.local.ip}")
    private String LocalIP;

    public void sendEmail(String toEmail, String name , String token){
        try{
        String verificationLink ="http://"+LocalIP+"/api/v1/confirm?token="+token;
            SimpleMailMessage message=new SimpleMailMessage();
             message.setTo(toEmail);
            message.setSubject("Verify Account\n");
            message.setText("Hello "+name+",\n\nPlease verifyLogin your email "+verificationLink);
            javaMailSender.send(message);
            System.out.println("✅ Email sent successfully to: " + toEmail);
        }
        catch (Exception e){
            System.out.println(e);
        }
    }
}
