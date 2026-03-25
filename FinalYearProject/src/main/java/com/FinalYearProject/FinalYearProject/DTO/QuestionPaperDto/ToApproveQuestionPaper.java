package com.FinalYearProject.FinalYearProject.DTO.QuestionPaperDto;

import com.FinalYearProject.FinalYearProject.DTO.QuestionDto.QuestionDTO;

import java.util.List;

public class ToApproveQuestionPaper {
    List<QuestionDTO> questionDTOList;
    String comment;

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public List<QuestionDTO> getQuestionDTOList() {
        return questionDTOList;
    }

    public void setQuestionDTOList(List<QuestionDTO> questionDTOList) {
        this.questionDTOList = questionDTOList;
    }
}
