package com.FinalYearProject.FinalYearProject.DTO;

import com.FinalYearProject.FinalYearProject.Domain.Question;

public class DtoForEmailAndQuestionInRequest {
    String email;
    Question question;

    public DtoForEmailAndQuestionInRequest(){}

    public DtoForEmailAndQuestionInRequest(String email, Question question){
        this.email=email;
        this.question=question;
    }

    public Question getQuestion() {
        return question;
    }

    public String getEmail(){
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setQuestion(Question question) {
        this.question = question;
    }
}
