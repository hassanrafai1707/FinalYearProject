package com.FinalYearProject.FinalYearProject.DTO.UserDto;

public class DtoForIdAndAdminPasswordInRequest {
    Long id;
    String adminPassword;

    public DtoForIdAndAdminPasswordInRequest() {}

    public DtoForIdAndAdminPasswordInRequest(Long id,String adminPassword){
        this.id=id;
        this.adminPassword=adminPassword;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAdminPassword() {
        return adminPassword;
    }

    public void setAdminPassword(String adminPassword) {
        this.adminPassword = adminPassword;
    }
}
