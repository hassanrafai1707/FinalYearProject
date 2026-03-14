package com.FinalYearProject.FinalYearProject.Util;

import com.FinalYearProject.FinalYearProject.Domain.Question;

import java.lang.reflect.Array;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

/**
 * QuestionUtil - Utility Class for Question Content Processing and Validation
 * PURPOSE: Provides utility methods for question content hashing and validation to ensure question quality and enable duplicate detection.
 * CONTENT FINGERPRINTING: sha256 method generates SHA-256 hash of question body text. Creates unique identifier for duplicate detection and content integrity.
 * DUPLICATE PREVENTION: Hash serves as question title/fingerprint in Question entity. Enables efficient duplicate question detection during creation.
 * CONTENT VALIDATION: checkIfQuestionBodyIsAcceptable validates question body text quality. Checks if spaces exceed 25% of text length (crude quality metric).
 * QUALITY CONTROL: Prevents low-quality questions with excessive whitespace or minimal content. Basic spam/precision check before question acceptance.
 * HASHING IMPLEMENTATION: Uses Java's MessageDigest for SHA-256. Converts byte array to hexadecimal string representation.
 * SECURITY CONSIDERATIONS: SHA-256 provides cryptographic strength suitable for content fingerprinting. Collision-resistant for practical purposes.
 * PERFORMANCE: Single-pass hash computation. Efficient for typical question lengths (50-500 characters).
 * INTEGRATION: Used by QuestionService for duplicate detection (question title generation) and content validation during question creation.
 * EXTENSION POINTS: Could add more sophisticated validation (minimum word count, profanity filtering, grammar checking, keyword extraction).
 * DEBUGGING: Consider logging hash values for troubleshooting duplicate detection issues in development.
 */
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

    public static Boolean checkIfQuestionBodyIsAcceptable(String questionBody){
        int counter =0;

        for (int i = 0; i < questionBody.length(); i++) {
            if (questionBody.charAt(i)==' '){
                counter++;
            }
        }
        if (counter>=questionBody.length()/4){
            return Boolean.FALSE;
        }
        else {
            return Boolean.TRUE;
        }
    }

    public static Boolean DepartmentCheck(List<Question> questions) {
        List<String> dep=questions.stream().map(Question::getDepartment).distinct().toList();
        if (dep.size()==1&&dep.getFirst().equals(UserUtil.getUserAuthentication().getUser().getDepartment())){
            return true;
        }
        else {
            return false;
        }
    }

    public static Boolean DepartmentCheck(Question question){
        if (question.getDepartment().equals(UserUtil.getUserAuthentication().getUser().getDepartment())){
            return true;
        }
        return false;
    }
}
