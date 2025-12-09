package com.FinalYearProject.FinalYearProject.Service;

import com.FinalYearProject.FinalYearProject.Domain.User;
import com.FinalYearProject.FinalYearProject.Exceptions.QuestionException.DuplicateQuestionException;
import com.FinalYearProject.FinalYearProject.Exceptions.QuestionException.QuestionNotFoundException;
import com.FinalYearProject.FinalYearProject.Exceptions.QuestionException.UnacceptableQuestion;
import com.FinalYearProject.FinalYearProject.Exceptions.UserEeceptions.UserNotAuthorizesException;
import com.FinalYearProject.FinalYearProject.Domain.Question;
import com.FinalYearProject.FinalYearProject.Repository.QuestionRepository;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

//TODO Use all unused methods in this class
//TODO Use cache to reduce time for retrieving and computing data
@Service
@AllArgsConstructor
public class QuestionService {

    @Autowired
    private QuestionRepository questionRepository;
    @Autowired
    private UserService userService;
    @Autowired
    private RedisService redisService;

    public List<Question> getAllQuestion(){
        List<Question> tempQuestion=questionRepository.findAll();
        if (!(tempQuestion.isEmpty())){
            return tempQuestion;
        }
        else {
            throw new QuestionNotFoundException("no Question in DataBase");
        }
    }
    public Question getQuestionById(Long id) {
        return questionRepository.findById(id)
                .orElseThrow(() -> new QuestionNotFoundException("Question not found with ID: " + id));
    }

    public List<Question> findBySubjectCode(String subjectCode){
        List<Question> tempQuestion=questionRepository.findBySubjectCode(subjectCode);
        if (!(tempQuestion.isEmpty())){
            return tempQuestion;
        }
        throw new QuestionNotFoundException("No questions found with Subject code: "+subjectCode);
    }

    public List<Question> findBySubjectCodeMappedCO(String subjectCode , String mappedCO){
        List<Question> tempQuestion=questionRepository.findBySubjectCodeAndMappedCO(subjectCode,mappedCO);
        if (!(tempQuestion.isEmpty())){
            return tempQuestion;
        }
        throw  new QuestionNotFoundException("No questions found with Subject name: "+subjectCode+"and Mapped CO"+mappedCO);
    }


    public List<Question> findBySubjectCodeMappedCOCognitiveLevel(String subjectCode, String mappedCO, String cognitiveLevel){
        List<Question> tempQuestions=questionRepository.findBySubjectCodeAndMappedCOAndCognitiveLevel(subjectCode, mappedCO, cognitiveLevel);
        if (!(tempQuestions.isEmpty())){
            return tempQuestions;
        }
        throw  new QuestionNotFoundException("No questions found with Subject name: "+subjectCode+"and Mapped CO"+mappedCO+"Cognitive level"+cognitiveLevel);
    }

    public List<Question> findBySubjectName(String subjectName){
        List<Question> tempQuestion=questionRepository.findBySubjectName(subjectName);
        if (!(tempQuestion.isEmpty())){
            return tempQuestion;
        }
        throw new QuestionNotFoundException("No questions found with Subject name: "+subjectName);
    }

    public List<Question> findBySubjectNameMappedCO(String subjectName,String mappedCO){
        List<Question> tempQuestion=questionRepository.findBySubjectNameAndMappedCO(subjectName,mappedCO);
        if (!(tempQuestion.isEmpty())){
            return tempQuestion;
        }
        throw  new QuestionNotFoundException("No questions found with Subject name: "+subjectName+"and Mapped CO"+mappedCO);
    }

    public List<Question> findBySubjectNameMappedCOCognitiveLevel(String subjectName,String mappedCO,String cognitiveLevel){
        List<Question> tempQuestion=questionRepository.findBySubjectNameAndMappedCOAndCognitiveLevel(subjectName, mappedCO, cognitiveLevel);
        if (!(tempQuestion.isEmpty())){
            return tempQuestion;
        }
        throw  new QuestionNotFoundException("No questions found with Subject name: "+subjectName+"and Mapped CO"+mappedCO+"Cognitive level"+cognitiveLevel);
    }

    public List<Question> findByCreatedByUsingEmail(String email){
        User tempUser= userService.findByEmail(email);
        if (!tempUser.getRole().equalsIgnoreCase("ROLE_TEACHER")) {
            throw new UserNotAuthorizesException("User with email is not authorized to make questions"+email);
        }
        else {
            List<Question> tempQuestion=questionRepository.findByCreatedByUsingEmail(email);
            if (!(tempQuestion.isEmpty())){
                return tempQuestion;
            }
            else {
                throw new QuestionNotFoundException("User with the email has not created any Question"+email);
            }
        }
    }

    public Question findQuestionByQuestionBody(String questionBody){
        return questionRepository.
                findByQuestionTitle(
                        sha256(questionBody)
                ).orElseThrow(
                        ()-> new QuestionNotFoundException("no question with this body")
                );
    }

    public void deleteQuestionByQuestionBody(String questionBody){
        Question temp=questionRepository.findByQuestionTitle(
                sha256(questionBody)
        ).orElseThrow(
                ()-> new QuestionNotFoundException("no question with this body")
        );
        questionRepository.delete(temp);
    }
    public List<Question> findByCreatedByUsingId(Long Id){
        User tempUser=userService.findUserById(Id);
        if (!tempUser.getRole().equalsIgnoreCase("ROLE_TEACHER")) {
            throw new UserNotAuthorizesException("User with id is not authorized to make questions"+Id);
        }
        else {
            List<Question> tempQuestion=questionRepository.findByCreatedByUsingId(Id);
            if (!(tempQuestion.isEmpty())){
                return tempQuestion;
            }
            else {
                throw new QuestionNotFoundException("User with the Id has not created any Question"+Id);
            }
        }
    }

    public Question addQuestion(Question question) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email=authentication.getName();
        String questionTitle=sha256(question.getQuestionBody());
        System.out.println(email+"in security context holder");
        User user=userService.findByEmail(email);
        if (
                question.getQuestionBody().isEmpty()||
                !checkIfQuestionBodyIsAcceptable(question.getQuestionBody())
        ){
            throw new UnacceptableQuestion("Unacceptable Question due to eather no question body or more spaces that words ");
        }
        if (
                questionRepository.existsByQuestionTitle(questionTitle)
        ){
            throw new DuplicateQuestionException("question already present");
        }
        else {
            // possible to remove this part in future as we are taking email from security context holder so no way to forge
            if (!user.getRole().equalsIgnoreCase("ROLE_TEACHER")){
                throw new UserNotAuthorizesException("User UnAuthorised to make this request");
            }
            else {
                question.setQuestionTitle(questionTitle);
                question.setCreatedBy(user);
                question.setInUse(false);
                return questionRepository.save(question);
            }
        }
    }

    public void deleteQuestionById(Long id){
        if (questionRepository.existsById(id)){
            questionRepository.deleteById(id);
        }
        else {
            throw new QuestionNotFoundException("Question not found with ID: " + id);
        }
    }

    public ArrayList<Question> generateBySubjectCodeQuestion(
            String subjectCode,
            String[] mappedCOs,
            int numberOfCognitiveLevel_A,
            int numberOfCognitiveLevel_R,
            int numberOfCognitiveLevel_U,
            int maxNumberOf2Marks,
            int maxNumberOf4Marks
    ) {
        Question[] allowed = questionRepository.findValidQuestionsWithSubjectCode(subjectCode, mappedCOs);
        ArrayList<Question> output = new ArrayList<>();
        Random random = new Random();

        if (allowed == null || allowed.length == 0) {
            throw new QuestionNotFoundException("No questions found for selected subject + CO");
        }

        while (
                (numberOfCognitiveLevel_A > 0 ||
                        numberOfCognitiveLevel_R > 0 ||
                        numberOfCognitiveLevel_U > 0)
                        &&
                        (maxNumberOf2Marks > 0 || maxNumberOf4Marks > 0)
        ) {

            int nextRandomQuestionIndex = random.nextInt(allowed.length);
            Question temp = allowed[nextRandomQuestionIndex];

            // Skip if already selected
            if (Boolean.TRUE.equals(temp.getIsInUse())) continue;

            switch (temp.getCognitiveLevel()) {
                case "A" -> {
                    if (numberOfCognitiveLevel_A > 0 &&
                            ((temp.getQuestionMarks() == 2 && maxNumberOf2Marks > 0) ||
                                    (temp.getQuestionMarks() == 4 && maxNumberOf4Marks > 0))) {

                        temp.setIsInUse(true);
                        output.add(temp);
                        numberOfCognitiveLevel_A--;

                        if (temp.getQuestionMarks() == 2) maxNumberOf2Marks--;
                        else maxNumberOf4Marks--;
                    }
                }

                case "R" -> {
                    if (numberOfCognitiveLevel_R > 0 &&
                            ((temp.getQuestionMarks() == 2 && maxNumberOf2Marks > 0) ||
                                    (temp.getQuestionMarks() == 4 && maxNumberOf4Marks > 0))) {

                        temp.setIsInUse(true);
                        output.add(temp);
                        numberOfCognitiveLevel_R--;

                        if (temp.getQuestionMarks() == 2) maxNumberOf2Marks--;
                        else maxNumberOf4Marks--;
                    }
                }

                case "U" -> {
                    if (numberOfCognitiveLevel_U > 0 &&
                            ((temp.getQuestionMarks() == 2 && maxNumberOf2Marks > 0) ||
                                    (temp.getQuestionMarks() == 4 && maxNumberOf4Marks > 0))) {

                        temp.setIsInUse(true);
                        output.add(temp);
                        numberOfCognitiveLevel_U--;

                        if (temp.getQuestionMarks() == 2) maxNumberOf2Marks--;
                        else maxNumberOf4Marks--;
                    }
                }
            }
        }
        return output;
    }


    public ArrayList<Question> generateBySubjectNameQuestion(
            String subjectName,
            String[] mappedCOs,
            int numberOfCognitiveLevel_A,
            int numberOfCognitiveLevel_R,
            int numberOfCognitiveLevel_U,
            int maxNumberOf2Marks,
            int maxNumberOf4Marks
    ) {
        Question[] allowed = questionRepository.findValidQuestionWithSubjectName(subjectName, mappedCOs);
        ArrayList<Question> output = new ArrayList<>();
        Random random = new Random();

        if (allowed == null || allowed.length == 0) {
            throw new QuestionNotFoundException("No questions found for selected subject + CO");
        }

        while (
                (numberOfCognitiveLevel_A > 0 ||
                        numberOfCognitiveLevel_R > 0 ||
                        numberOfCognitiveLevel_U > 0)
                        &&
                        (maxNumberOf2Marks > 0 || maxNumberOf4Marks > 0)
        ) {

            int nextRandomQuestionIndex = random.nextInt(allowed.length);
            Question temp = allowed[nextRandomQuestionIndex];

            // Skip if already selected
            if (Boolean.TRUE.equals(temp.getIsInUse())) continue;

            switch (temp.getCognitiveLevel()) {
                case "A" -> {
                    if (numberOfCognitiveLevel_A > 0 &&
                            ((temp.getQuestionMarks() == 2 && maxNumberOf2Marks > 0) ||
                                    (temp.getQuestionMarks() == 4 && maxNumberOf4Marks > 0))) {

                        temp.setIsInUse(true);
                        output.add(temp);
                        numberOfCognitiveLevel_A--;

                        if (temp.getQuestionMarks() == 2) maxNumberOf2Marks--;
                        else maxNumberOf4Marks--;
                    }
                }

                case "R" -> {
                    if (numberOfCognitiveLevel_R > 0 &&
                            ((temp.getQuestionMarks() == 2 && maxNumberOf2Marks > 0) ||
                                    (temp.getQuestionMarks() == 4 && maxNumberOf4Marks > 0))) {

                        temp.setIsInUse(true);
                        output.add(temp);
                        numberOfCognitiveLevel_R--;

                        if (temp.getQuestionMarks() == 2) maxNumberOf2Marks--;
                        else maxNumberOf4Marks--;
                    }
                }

                case "U" -> {
                    if (numberOfCognitiveLevel_U > 0 &&
                            ((temp.getQuestionMarks() == 2 && maxNumberOf2Marks > 0) ||
                                    (temp.getQuestionMarks() == 4 && maxNumberOf4Marks > 0))) {

                        temp.setIsInUse(true);
                        output.add(temp);
                        numberOfCognitiveLevel_U--;

                        if (temp.getQuestionMarks() == 2) maxNumberOf2Marks--;
                        else maxNumberOf4Marks--;
                    }
                }
            }
        }
        return output;
    }

    private Boolean checkIfQuestionBodyIsAcceptable(String questionBody){
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

    private String sha256(String input){
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
