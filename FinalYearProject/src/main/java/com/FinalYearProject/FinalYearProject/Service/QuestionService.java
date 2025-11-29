package com.FinalYearProject.FinalYearProject.Service;

import com.FinalYearProject.FinalYearProject.Domain.Question;
import com.FinalYearProject.FinalYearProject.Domain.User;
import com.FinalYearProject.FinalYearProject.Repository.QuestionRepository;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
@AllArgsConstructor
public class QuestionService {

    @Autowired
    private QuestionRepository questionRepository;
    @Autowired
    private UserService userService;

    public Question getQuestionById(Long id) {
        return questionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Question not found with ID: " + id));
    }

    public List<Question> findByMappedCO(String mappedCO){
        return questionRepository.findByMappedCO(mappedCO);
    }

    public List<Question> findBySubjectName(String subjectName){
        return questionRepository.findBySubjectName(subjectName);
    }

    public List<Question> findBySubjectCode(String subjectCode){
        return questionRepository.findBySubjectCode(subjectCode);
    }

    public List<Question> findByCognitiveLevel(String cognitiveLevel){
        return questionRepository.findByCognitiveLevel(cognitiveLevel);
    }

    public List<Question> findByCreatedBy(User user){
        if (userService.existsByEmail(user.getEmail())) {
            return questionRepository.findByCreatedBy(user);
        }
        throw new RuntimeException("User does not exist!");
    }

    public Question addQuestion(Question question){
        question.setInUse(false);
        return questionRepository.save(question);
    }

    public void deleteQuestionById(Long id){
        if (questionRepository.existsById(id)){
            questionRepository.deleteById(id);
        }
        else {
            throw new RuntimeException("Question not found with ID: " + id);
        }
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
