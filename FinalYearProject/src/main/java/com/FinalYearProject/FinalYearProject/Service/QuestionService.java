package com.FinalYearProject.FinalYearProject.Service;

import com.FinalYearProject.FinalYearProject.Exceptions.QuestionException.QuestionExistsException;
import com.FinalYearProject.FinalYearProject.Exceptions.QuestionException.QuestionNotFoundException;
import com.FinalYearProject.FinalYearProject.Exceptions.UserEeceptions.UserNotAuthorizesException;
import com.FinalYearProject.FinalYearProject.Domain.Question;
import com.FinalYearProject.FinalYearProject.Repository.QuestionRepository;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
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
        return questionRepository.findAll();
    }
    public Question getQuestionById(Long id) {
        return questionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Question not found with ID: " + id));
    }

    public List<Question> findByMappedCO(String mappedCO){
        List<Question> questions=questionRepository.findByMappedCO(mappedCO)
                .orElseThrow(()-> new QuestionNotFoundException("Question not found with mappingCo"+mappedCO));
        return questions;
    }

    public List<Question> findBySubjectName(String subjectName){
        List<Question> questions=questionRepository.findBySubjectName(subjectName)
                .orElseThrow(()->new QuestionNotFoundException("No questions found with Subject name: "+subjectName));

        return questions;
    }

    public List<Question> findBySubjectCode(String subjectCode){
        List<Question> questions=questionRepository.findBySubjectCode(subjectCode)
                .orElseThrow(()->new QuestionNotFoundException("No questions found with Subject code: "+subjectCode));

        return questions;
    }

    public List<Question> findByCognitiveLevel(String cognitiveLevel){
        List<Question> questions=questionRepository.findByCognitiveLevel(cognitiveLevel)
                .orElseThrow(()-> new QuestionNotFoundException("No questions found with Cognitive Level: " + cognitiveLevel));

        return questions;
    }

    public List<Question> findByCreatedByUsingEmail(String email){
        if (userService.existsByEmail(email)) {
            return questionRepository.findByCreatedByUsingEmail(email)
                    .orElseThrow(()-> new QuestionNotFoundException("User with the email has not created any Question"));
        }
        throw new UsernameNotFoundException("User does not exist! with email"+email);
    }

    public List<Question> findByCreatedByUsingId(Long Id){
        if (userService.existsById(Id)){
            return questionRepository.findByCreatedByUsingId(Id)
                    .orElseThrow(()-> new QuestionNotFoundException("User with the Id has not created any Question"));
        }
        throw new UsernameNotFoundException("User does not exist! with Id"+Id);
    }

    public Question addQuestion(Question question) {
        if (
                questionRepository.existsByQuestionBody(question.getQuestionBody())){
            throw new QuestionExistsException("question already present");
        }
        else {
            if (question.getCreatedBy().getRole().equalsIgnoreCase("ROLE_TEACHER")){
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

    public void deleteQuestionByQuestionBody(String questionBody){
        Question temp=questionRepository.findByQuestionBody(questionBody)
                .orElseThrow(
                        ()-> new QuestionNotFoundException(
                            "Question with this "+questionBody+" Question body not present"
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
