package com.FinalYearProject.FinalYearProject.Service;

import com.FinalYearProject.FinalYearProject.Config.IpConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;
import org.springframework.mail.javamail.JavaMailSender;

@Service
@RequiredArgsConstructor
@Slf4j
// this class is used to send Email
// using diynimic ip
public class ConformationService {
    @Autowired
    public JavaMailSender javaMailSender;
    @Autowired
    private IpConfig ipConfig;

    public void sendEmail(String toEmail, String name , String token){
        try{
            String LocalIP= ipConfig.getLocalIp();
        String verificationLink ="https://"+LocalIP+"/api/v1/user/confirm?token="+token;
            SimpleMailMessage message=new SimpleMailMessage();
             message.setTo(toEmail);
            message.setSubject("Verify Account");
            message.setText("Hello"+name+",\n\nPlease verifyLogin your email "+verificationLink);
            javaMailSender.send(message);
        }
        catch (Exception e){
            System.out.println(e);
        }
    }
}
