package com.FinalYearProject.FinalYearProject.Util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

public class QuestionPaperUtil {
    private QuestionPaperUtil(){}

    public static String sha256FingerPrintUsingIds(List<Long> Ids){
        try{
            String temp=Ids.toString();
            System.out.println(temp);
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashCode=md.digest(temp.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex=new StringBuilder();
            for (byte h :hashCode){
                hex.append(String.format("%02x",h));
            }
            System.out.println(hex);
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

}
