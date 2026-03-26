package com.FinalYearProject.FinalYearProject.DTO.QuestionPaperDto;

public class IdAndComment {
    Long id;
    String comment;

    public IdAndComment() {}

    public IdAndComment(Long id,String comment){
        this.comment=comment;
        this.id=id;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
