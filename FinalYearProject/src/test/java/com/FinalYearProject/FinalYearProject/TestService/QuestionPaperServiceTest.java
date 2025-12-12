package com.FinalYearProject.FinalYearProject.TestService;

import com.FinalYearProject.FinalYearProject.Domain.Question;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

@ExtendWith(MockitoExtension.class)
public class QuestionPaperServiceTest {

    private Set<Question> questionSet = new HashSet<>();

    @Test
    public void sha256Test(){
        Question q1 = new Question();
        q1.setId(1L);
        q1.setSubjectName("Java");
        q1.setSubjectCode("3140703");
        q1.setMappedCO("CO3");
        q1.setCognitiveLevel("A");
        q1.setQuestionMarks(4);
        q1.setQuestionTitle("Intro Question");
        q1.setQuestionBody("Explain JVM");
        q1.setInUse(false);

        Question q2 = new Question();
        q2.setId(1L);
        q2.setSubjectName("Java");
        q2.setSubjectCode("3140703");
        q2.setMappedCO("CO3");
        q2.setCognitiveLevel("A");
        q2.setQuestionMarks(4);
        q2.setQuestionTitle("Intro Question");
        q2.setQuestionBody("Explain JVM");
        q2.setInUse(false);

        questionSet.add(q1);
        questionSet.add(q2);
        String combined=questionSet.stream()
                .sorted(Comparator.comparing(Question::getId))
                .map(q -> q.getId() + "|" +
                        q.getSubjectName() + "|" +
                        q.getSubjectCode() + "|" +
                        q.getMappedCO() + "|" +
                        q.getCognitiveLevel() + "|" +
                        q.getQuestionMarks() + "|" +
                        q.getQuestionTitle() + "|" +
                        q.getQuestionBody() + "|" +
                        q.getInUse()+"|"
                )
                .reduce("",String::concat);
        System.out.println(combined);
    }
}
