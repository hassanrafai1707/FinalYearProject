package com.FinalYearProject.FinalYearProject.Service;

import com.FinalYearProject.FinalYearProject.Domain.User;
import com.FinalYearProject.FinalYearProject.Exceptions.QuestionException.DuplicateQuestionException;
import com.FinalYearProject.FinalYearProject.Exceptions.QuestionException.QuestionNotFoundException;
import com.FinalYearProject.FinalYearProject.Exceptions.UserEeceptions.UserNotAuthorizesException;
import com.FinalYearProject.FinalYearProject.Domain.Question;
import com.FinalYearProject.FinalYearProject.Exceptions.UserEeceptions.UserNotFoundException;
import com.FinalYearProject.FinalYearProject.Repository.QuestionRepository;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.*;

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

    public List<Question> findByMappedCO(String subjectCode ,String mappedCO){
        List<Question> tempQuestion=questionRepository.findBySubjectCodeAndMappedCO(subjectCode,mappedCO);
        if (!(tempQuestion.isEmpty())){
            return tempQuestion;
        }
        else {
         throw new QuestionNotFoundException("Question not found with mappingCo"+mappedCO);
        }
    }

    public List<Question> findBySubjectName(String subjectName){
        List<Question> tempQuestion=questionRepository.findBySubjectName(subjectName);
        if (!(tempQuestion.isEmpty())){
            return tempQuestion;
        }
        throw new QuestionNotFoundException("No questions found with Subject name: "+subjectName);
    }

    public List<Question> findBySubjectCode(String subjectCode){
        List<Question> tempQuestion=questionRepository.findBySubjectCode(subjectCode);
        if (!(tempQuestion.isEmpty())){
            return tempQuestion;
        }
        else {
            throw new QuestionNotFoundException("No questions found with Subject code: "+subjectCode);
        }
    }

    public List<Question> findByCognitiveLevel(String subjectCode,String mappedCo, String cognitiveLevel){
        List<Question> tempQuestions=questionRepository.findBySubjectCodeAndMappedCOAndCognitiveLevel(subjectCode, mappedCo, cognitiveLevel);
        if (!(tempQuestions.isEmpty())){
            return tempQuestions;
        }
        else{
            throw new QuestionNotFoundException("No questions found with Cognitive Level: " + cognitiveLevel);
        }
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
    //TODO use hash code in this method of question body
    public Question addQuestion(Question question) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email=authentication.getName();
        System.out.println(email+"in security context holder");
        User user=userService.findByEmail(email);
        if (email==null){
            throw new UserNotFoundException("User not found");
        }
        if (
                questionRepository.existsByQuestionTitle(question.getQuestionBody())){
            throw new DuplicateQuestionException("question already present");
        }
        else {
            if (user.getRole().equalsIgnoreCase("ROLE_TEACHER")){
                question.setCreatedBy(user);
                question.setInUse(false);
                return questionRepository.save(question);
            }
            else {
                throw new UserNotAuthorizesException("User UnAuthorised to make this request");
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

    public void deleteQuestionByQuestionTitle(String questionTitle){
        Question temp=questionRepository.findByQuestionTitle(questionTitle)
                .orElseThrow(
                        ()-> new QuestionNotFoundException(
                            "Question with this "+questionTitle+" Question body not present"
                        )
                );
        questionRepository.delete(temp);
    }

    public List<Question> generateQuestion(
            String[] selectedMappedCO,
            int numberOfCognitiveLevel_L,
            int numberOfCognitiveLevel_R,
            int numberOfCognitiveLevel_U,
            int max2Marks,
            int max4Marks,
            int maxMarks
    ) {

        List<Question> allowed = questionRepository.findAll()
                .stream()
                .filter(q -> !q.getInUse() &&
                        checkIfPartOfSelectedMappedCO(q, selectedMappedCO))
                .toList();

        Collections.shuffle(allowed);

        List<Question> output = new ArrayList<>();
        int totalMarks = 0;
        int selected2M = 0;
        int selected4M = 0;

        for (Question q : allowed) {

            if (totalMarks >= maxMarks) break;

            int marks = q.getQuestionMarks();
            String level = q.getCognitiveLevel().trim().toUpperCase();
            boolean canPick = false;

            switch (level) {
                case "L" -> canPick = numberOfCognitiveLevel_L > 0;
                case "R" -> canPick = numberOfCognitiveLevel_R > 0;
                case "U" -> canPick = numberOfCognitiveLevel_U > 0;
            }

            if (marks == 2 && selected2M < max2Marks) canPick = canPick && true;
            else if (marks == 4 && selected4M < max4Marks) canPick = canPick && true;
            else canPick = false;

            if (canPick) {
                output.add(q);
                totalMarks += marks;

                switch (level) {
                    case "L" -> numberOfCognitiveLevel_L--;
                    case "R" -> numberOfCognitiveLevel_R--;
                    case "U" -> numberOfCognitiveLevel_U--;
                }

                if (marks == 2) selected2M++;
                if (marks == 4) selected4M++;

                if (numberOfCognitiveLevel_L == 0 &&
                        numberOfCognitiveLevel_R == 0 &&
                        numberOfCognitiveLevel_U == 0 &&
                        selected2M == max2Marks &&
                        selected4M == max4Marks)
                    break;
            }
        }
        return output;
    }

    private Boolean checkIfPartOfSelectedMappedCO(
            Question temp, String[] selectedMappedCO){
        return Arrays.stream(selectedMappedCO)
                .anyMatch(co -> temp.getMappedCO().equalsIgnoreCase(co));
    }
}
