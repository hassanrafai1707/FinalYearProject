package com.FinalYearProject.FinalYearProject.DTO;

public class DtoForEmaiAndIdInRequest {
    Long Id;
    String email;

    public DtoForEmaiAndIdInRequest(){}

    public DtoForEmaiAndIdInRequest(Long Id, String email){
        this.Id=Id;
        this.email=email;
    }

    public void setId(Long Id){
        this.Id=Id;
    }

    public void setEmail(String email){
        this.email=email;
    }
    public Long getId(){
        return Id;
    }

    public String getEmail(){
        return email;
    }
}
