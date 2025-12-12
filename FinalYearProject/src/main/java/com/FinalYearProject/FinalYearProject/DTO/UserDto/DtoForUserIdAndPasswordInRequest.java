package com.FinalYearProject.FinalYearProject.DTO.UserDto;

public class DtoForUserIdAndPasswordInRequest {
    Long Id;
    String password;

    public DtoForUserIdAndPasswordInRequest(){}

    public DtoForUserIdAndPasswordInRequest(Long Id, String password){
        this.Id=Id;
        this.password=password;
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
}
