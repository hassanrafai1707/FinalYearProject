package com.FinalYearProject.FinalYearProject.Util;

import com.FinalYearProject.FinalYearProject.DTO.QuestionDto.QuestionDTO;
import com.FinalYearProject.FinalYearProject.Domain.Question;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.stream.Collectors;

public class QuestionDtoUtil {

    private QuestionDtoUtil() {}
    public static QuestionDTO QuestionToQuestionDto(Question question){
        return new QuestionDTO(
                question.getId(),
                question.getSubjectName(),
                question.getQuestionMarks(),
                question.getMappedCO(),
                question.getSubjectCode(),
                question.getCognitiveLevel(),
                question.getQuestionBody()
        );
    }

    public static List<QuestionDTO> listOfQuestionToQuestionDto(List<Question> questions){
        if (questions.isEmpty()){
            throw new IllegalArgumentException("the question passed in this method can not be null");
        }
        return questions.stream().map(question -> new QuestionDTO(
                question.getId(),
                question.getSubjectName(),
                question.getQuestionMarks(),
                question.getMappedCO(),
                question.getSubjectCode(),
                question.getCognitiveLevel(),
                question.getQuestionBody()
        )).collect(Collectors.toList());
    }

    public static Page<QuestionDTO> questionToQuestionDTO_Paged(Page<Question> questionPage){

        return questionPage.map(question -> new QuestionDTO(
                question.getId(),
                question.getSubjectName(),
                question.getQuestionMarks(),
                question.getMappedCO(),
                question.getSubjectCode(),
                question.getCognitiveLevel(),
                question.getQuestionBody()
        ));
    }

    public static PageImpl<QuestionDTO> questionToQuestionDTO_Paged(
            List<Question> questions,
            int pageNo,
            int size
    ){
        if (questions.isEmpty()){
            throw new IllegalArgumentException("the question passed in this method can not be null");
        }
        return new PageImpl<>(
                questions.stream().map(question -> new QuestionDTO(
                        question.getId(),
                        question.getSubjectName(),
                        question.getQuestionMarks(),
                        question.getMappedCO(),
                        question.getSubjectCode(),
                        question.getCognitiveLevel(),
                        question.getQuestionBody()
                )).toList(),
                PageRequest.of(pageNo,size),
                questions.size()
        );
    }
}
