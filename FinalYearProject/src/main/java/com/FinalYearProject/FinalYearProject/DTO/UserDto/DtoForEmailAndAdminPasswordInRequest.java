package com.FinalYearProject.FinalYearProject.DTO.UserDto;

public class DtoForEmailAndAdminPasswordInRequest {
    String email;
    String adminPassword;

    public DtoForEmailAndAdminPasswordInRequest(){}

    public DtoForEmailAndAdminPasswordInRequest(String email,String adminPassword){
        this.email=email;
        this.adminPassword=adminPassword;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAdminPassword() {
        return adminPassword;
    }

    public void setAdminPassword(String adminPassword) {
        this.adminPassword = adminPassword;
    }
}
