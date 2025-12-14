package com.FinalYearProject.FinalYearProject.DTO.UserDto;

public class DtoForEmailAndPasswordInRequest {
    String email;
    String password;
    String adminPassword;

    public DtoForEmailAndPasswordInRequest(){}

    public DtoForEmailAndPasswordInRequest(String email, String password ,String adminPassword){
        this.email=email;
        this.password=password;
        this.adminPassword=adminPassword;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getAdminPassword() {
        return adminPassword;
    }

    public void setAdminPassword(String adminPassword) {
        this.adminPassword = adminPassword;
    }
}
