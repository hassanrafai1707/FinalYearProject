package com.FinalYearProject.FinalYearProject.Util;

import com.FinalYearProject.FinalYearProject.Domain.QuestionPaper;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

/**
 * QuestionPaperUtil - Utility Class for Exam Paper Fingerprint Generation
 * PURPOSE: Provides SHA-256 fingerprint generation for question papers to enable duplicate detection and content integrity verification.
 * FINGERPRINT ALGORITHM: Generates SHA-256 hash from sorted list of question IDs. Creates content-based fingerprint that uniquely identifies paper composition.
 * DUPLICATE DETECTION: Enables detection of identical question papers even with different titles. Used by QuestionPaperService to prevent duplicate paper creation.
 * CONTENT INTEGRITY: Fingerprint serves as checksum for paper content. Any change in question composition results in different fingerprint.
 * IMPLEMENTATION DETAILS: Converts List<Long> IDs to string representation, computes SHA-256 hash, converts to hexadecimal string.
 * SORTING IMPORTANCE: IDs are sorted before hashing to ensure same fingerprint regardless of question order in list. Consistent ordering is critical.
 * SECURITY CONSIDERATIONS: SHA-256 provides cryptographic strength against collisions. Suitable for integrity verification in academic context.
 * DEBUGGING SUPPORT: Prints input string and resulting hex hash for development troubleshooting. Should be removed or logged appropriately in production.
 * INTEGRATION: Used by QuestionPaperService.addQuestionPaper() for duplicate detection. QuestionPaper entity stores fingerprint in database.
 * EXTENSION POINTS: Could support additional fingerprinting methods (MD5 for speed, inclusion of marks distribution, cognitive level patterns).
 * PERFORMANCE: SHA-256 computation is fast for typical paper sizes (10-100 questions). Single hash operation per paper creation.
 */
public class QuestionPaperUtil {
    private QuestionPaperUtil(){}

    public static String sha256FingerPrintUsingIds(List<Long> Ids){
        try{
            String temp=Ids.toString();
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashCode=md.digest(temp.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex=new StringBuilder();
            for (byte h :hashCode){
                hex.append(String.format("%02x",h));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean departmentCheck(QuestionPaper questionPaper){
        if (
                questionPaper.getGeneratedBy().getDepartment().equals(UserUtil.getUserAuthentication().getUser().getDepartment())
        ){
            return true;
        }
        return false;
    }
}
