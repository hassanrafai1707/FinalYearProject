package com.FinalYearProject.FinalYearProject.Util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class QuestionUtil {

    private QuestionUtil(){}

    public static String sha256(String input){
        try {
            MessageDigest messageDigest =MessageDigest.getInstance("SHA-256");
            byte[] encoderHash = messageDigest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder temp= new StringBuilder();
            for (byte b : encoderHash){
                temp.append(String.format("%02x",b));
            }
            return temp.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
