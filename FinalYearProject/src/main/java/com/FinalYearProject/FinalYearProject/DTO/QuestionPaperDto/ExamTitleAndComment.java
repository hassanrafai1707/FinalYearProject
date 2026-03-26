package com.FinalYearProject.FinalYearProject.DTO.QuestionPaperDto;

public class ExamTitleAndComment {
    String examTitle;
    String comment;

    public ExamTitleAndComment(){}

    public ExamTitleAndComment(String examTitle,String comment) {
        this.examTitle = examTitle;
        this.comment=comment;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getExamTitle() {
        return examTitle;
    }

    public void setExamTitle(String examTitle) {
        this.examTitle = examTitle;
    }
}
