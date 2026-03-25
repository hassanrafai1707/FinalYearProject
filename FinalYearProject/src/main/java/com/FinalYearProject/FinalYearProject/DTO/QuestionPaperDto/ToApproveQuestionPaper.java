package com.FinalYearProject.FinalYearProject.DTO.QuestionPaperDto;

import com.FinalYearProject.FinalYearProject.DTO.QuestionDto.QuestionDTO;

import java.util.List;

public class ToApproveQuestionPaper {
    List<QuestionDTO> questionDTOList;
    String examTitle;

    public String getExamTitle() {
        return examTitle;
    }

    public void setExamTitle(String examTitle) {
        this.examTitle = examTitle;
    }

    public List<QuestionDTO> getQuestionDTOList() {
        return questionDTOList;
    }

    public void setQuestionDTOList(List<QuestionDTO> questionDTOList) {
        this.questionDTOList = questionDTOList;
    }
}
