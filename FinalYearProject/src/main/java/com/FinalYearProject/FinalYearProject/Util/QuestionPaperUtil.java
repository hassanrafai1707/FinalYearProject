package com.FinalYearProject.FinalYearProject.Util;

import com.FinalYearProject.FinalYearProject.Domain.Question;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Comparator;
import java.util.Set;

public class QuestionPaperUtil {
    private QuestionPaperUtil(){}
    public static String sha256(Set<Question> questions) {
        try {
            // 1. Convert each question to a stable string representation
            String combined = questions.stream()
                    .sorted(Comparator.comparing(Question::getId)) // sort to make deterministic
                    .map(q -> q.getId() + "|" +
                            q.getSubjectName() + "|" +
                            q.getSubjectCode() + "|" +
                            q.getMappedCO() + "|" +
                            q.getCognitiveLevel() + "|" +
                            q.getQuestionMarks() + "|" +
                            q.getQuestionTitle() + "|" +
                            q.getQuestionBody() + "|" +
                            q.getInUse())
                    .reduce("",String::concat);
            System.out.println(combined);
            // 2. Hash the combined string
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(combined.getBytes(StandardCharsets.UTF_8));

            // 3. Convert hash to hex
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) {
                hex.append(String.format("%02x", b));
            }

            return hex.toString();

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

}
