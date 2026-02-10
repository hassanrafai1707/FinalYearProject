package com.FinalYearProject.FinalYearProject.Util;

import com.FinalYearProject.FinalYearProject.DTO.QuestionDto.QuestionDTO;
import com.FinalYearProject.FinalYearProject.Domain.Question;

import java.util.ArrayList;
import java.util.List;

public class QuestionDtoUtil {

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
        List<QuestionDTO> temp=new ArrayList<>();
        for (Question question:questions){
            temp.add(
                    new QuestionDTO(
                            question.getId(),
                            question.getSubjectName(),
                            question.getQuestionMarks(),
                            question.getMappedCO(),
                            question.getSubjectCode(),
                            question.getCognitiveLevel(),
                            question.getQuestionBody()
                    )
            );
        }
        return temp;
    }
}
