package com.FinalYearProject.FinalYearProject.Util;

import com.FinalYearProject.FinalYearProject.DTO.QuestionPaperDto.QuestionPaperDto;
import com.FinalYearProject.FinalYearProject.Domain.QuestionPaper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.stream.Collectors;

public class QuestionPaperDtoUtil {
    private QuestionPaperDtoUtil(){};

    public static QuestionPaperDto questionPaperToQuestionPaperDto(QuestionPaper questionPaper){
        return new QuestionPaperDto(
                questionPaper.getId(),
                questionPaper.getExamTitle(),
                questionPaper.getGeneratedBy(),
                questionPaper.getApproved(),
                questionPaper.getApprovedBy()
        );
    }

    public static List<QuestionPaperDto> listOfQuestionPaperToQuestionPaperDto(List<QuestionPaper> questionPapers){
        if (questionPapers.isEmpty()){
            throw new IllegalArgumentException("this function does not take empty list but it is getting an empty list");
        }
        return questionPapers.stream().map(questionPaper ->
                new QuestionPaperDto(
                        questionPaper.getId(),
                        questionPaper.getExamTitle(),
                        questionPaper.getGeneratedBy(),
                        questionPaper.getApproved(),
                        questionPaper.getApprovedBy()
                )
        ).collect(Collectors.toList());
    }

    public static PageImpl<QuestionPaperDto> questionPaperToQuestionPaperDto(Page<QuestionPaper> questionPapers,int pageNo, int size){
        return new PageImpl<>(
                questionPapers.getContent().stream().map(questionPaper -> new QuestionPaperDto(
                        questionPaper.getId(),
                        questionPaper.getExamTitle(),
                        questionPaper.getGeneratedBy(),
                        questionPaper.getApproved(),
                        questionPaper.getApprovedBy()
                )).collect(Collectors.toList()),
                PageRequest.of(pageNo,size),
                questionPapers.getTotalElements()
        );
    }
}
