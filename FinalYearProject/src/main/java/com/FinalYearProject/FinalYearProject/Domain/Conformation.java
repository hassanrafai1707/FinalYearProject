package com.FinalYearProject.FinalYearProject.Domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.Random;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@Getter
@Setter
public class Conformation {
    public String token;
    public int Otp;
    private User user;

    public Conformation(User user) {
        this.user = user;
        this.token= UUID.randomUUID().toString();
        generateOtp();
    }

    public User getUser() {
        return user;
    }

    public String getToken() {
        return token;
    }

    public void generateOtp(){
        Random random=new Random();
        int Otp=random.nextInt(1000,9999);
        this.Otp=Otp;
    }

    public int getOtp(){
        return Otp;
    }
}
