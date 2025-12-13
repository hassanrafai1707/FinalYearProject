package com.FinalYearProject.FinalYearProject.DTO.UserDto;

public class DtoForUserIdAndPasswordInRequest {
    Long Id;
    String password;
    String adminPassword;

    public DtoForUserIdAndPasswordInRequest(){}

    public DtoForUserIdAndPasswordInRequest(Long Id, String password,String adminPassword){
        this.Id=Id;
        this.password=password;
        this.adminPassword=adminPassword;
    }

    public Long getId() {
        return Id;
    }

    public void setId(Long id) {
        Id = id;
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
