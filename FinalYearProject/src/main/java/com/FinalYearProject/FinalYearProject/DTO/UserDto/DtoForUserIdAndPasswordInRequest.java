package com.FinalYearProject.FinalYearProject.DTO.UserDto;

public class DtoForUserIdAndPasswordInRequest {
    Long id;
    String password;
    String adminPassword;

    public DtoForUserIdAndPasswordInRequest(){}

    public DtoForUserIdAndPasswordInRequest(Long id, String password,String adminPassword){
        this.id =id;
        this.password=password;
        this.adminPassword=adminPassword;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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
