package com.FinalYearProject.FinalYearProject.DTO.UserDto;

import java.util.ArrayList;
import java.util.List;

public class DtoForEmailsAndPasswordInRequest {
    List<String> emails=new ArrayList<>();
    String adminPassword;

    public DtoForEmailsAndPasswordInRequest() {}

    public DtoForEmailsAndPasswordInRequest(String adminPassword,List<String> emails){
        this.adminPassword=adminPassword;
        this.emails=emails;
    }

    public List<String> getEmails() {
        return emails;
    }

    public void setEmails(List<String> emails) {
        this.emails = emails;
    }

    public String getAdminPassword() {
        return adminPassword;
    }

    public void setAdminPassword(String adminPassword) {
        this.adminPassword = adminPassword;
    }
}
