package com.FinalYearProject.FinalYearProject.DTO.UserDto;

public class DtoForRoleAndEmailAndPasswordInRequest {
    String email;
    String role;
    String password;

    public DtoForRoleAndEmailAndPasswordInRequest() {}

    public DtoForRoleAndEmailAndPasswordInRequest(String email, String role, String password){
        this.email=email;
        this.role=role;
        this.password=password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
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
}
