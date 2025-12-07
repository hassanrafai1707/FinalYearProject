package com.FinalYearProject.FinalYearProject.DTO.UserDto;

public class DtoForOldEmailAndNewEmailInRequest {
    String newEmail;
    String oldEmail;

    public DtoForOldEmailAndNewEmailInRequest(){}

    public DtoForOldEmailAndNewEmailInRequest(String newEmail, String oldEmail){
        this.newEmail=newEmail;
        this.oldEmail=oldEmail;
    }

    public void setNewEmail(String newEmail){
        this.newEmail=newEmail;
    }

    public void setOldEmail(String oldEmail){
        this.oldEmail=oldEmail;
    }

    public String getNewEmail(){
        return newEmail;
    }

    public String getOldEmail(){
        return oldEmail;
    }

}
