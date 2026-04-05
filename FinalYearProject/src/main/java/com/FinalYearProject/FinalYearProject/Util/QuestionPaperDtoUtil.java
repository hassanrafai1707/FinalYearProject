package com.FinalYearProject.FinalYearProject.Util;

import com.FinalYearProject.FinalYearProject.DTO.QuestionPaperDto.QuestionPaperDto;
import com.FinalYearProject.FinalYearProject.Domain.QuestionPaper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;

public class QuestionPaperDtoUtil {
    private QuestionPaperDtoUtil(){}

    public static QuestionPaperDto questionPaperToQuestionPaperDto(QuestionPaper questionPaper){
        return new QuestionPaperDto(
                questionPaper.getId(),
                questionPaper.getExamTitle(),
                questionPaper.getGeneratedBy(),
                questionPaper.getApproved(),
                questionPaper.getApprovedBy(),
                questionPaper.getComment(),
                questionPaper.getListOfQuestion()
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
                        questionPaper.getApprovedBy(),
                        questionPaper.getComment(),
                        questionPaper.getListOfQuestion()
                )
        ).toList();
    }

    public static Page<QuestionPaperDto> questionPaperToQuestionPaperDtoPaged(Page<QuestionPaper> questionPapers){
        if (questionPapers.isEmpty()||questionPapers.getContent().isEmpty()){
            throw new IllegalArgumentException("the question passed in this method can not be null");
        }
        return questionPapers.map(
                questionPaper ->new QuestionPaperDto(
                        questionPaper.getId(),
                        questionPaper.getExamTitle(),
                        questionPaper.getGeneratedBy(),
                        questionPaper.getApproved(),
                        questionPaper.getApprovedBy(),
                        questionPaper.getComment(),
                        questionPaper.getListOfQuestion()
                ));
    }

    public static PageImpl<QuestionPaperDto> questionPaperToQuestionPaperDtoPaged(
            List<QuestionPaper> questionPapers,
            int pageNo,
            int size
    ){
        if (questionPapers.isEmpty()){
            throw new IllegalArgumentException("the question passed in this method can not be null");
        }
        return new PageImpl<>(
                questionPapers.stream().map(questionPaper -> new QuestionPaperDto(
                        questionPaper.getId(),
                        questionPaper.getExamTitle(),
                        questionPaper.getGeneratedBy(),
                        questionPaper.getApproved(),
                        questionPaper.getApprovedBy(),
                        questionPaper.getComment(),
                        questionPaper.getListOfQuestion()
                )).toList(),
                PageRequest.of(pageNo,size),
                questionPapers.size()
        );
    }
}
