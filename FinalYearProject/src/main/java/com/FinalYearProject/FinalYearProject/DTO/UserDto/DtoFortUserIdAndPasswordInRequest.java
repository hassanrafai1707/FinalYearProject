package com.FinalYearProject.FinalYearProject.DTO.UserDto;

public class DtoFortUserIdAndPasswordInRequest {
    Long Id;
    String password;

    public DtoFortUserIdAndPasswordInRequest(){}

    public DtoFortUserIdAndPasswordInRequest(Long Id, String password){
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
