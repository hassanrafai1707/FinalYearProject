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

    public void sendEmail(String toEmail, String name , String token,int Otp){
        try{
            //example http://192.168.1.38:8080/api/v1/confirm?token=673651f2-b068-4334-a03b-a2891c12689d&OTP=3556
        String verificationLink ="http://"+LocalIP+":8080/api/v1/confirm?token="+token+"&OTP="+Otp;//need to spiffy port I am also passing the otp for essay login
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
