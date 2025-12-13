package com.FinalYearProject.FinalYearProject.DTO.UserDto;

public class DtoForRoleAndEmailInRequest {
    String email;
    String role;

    public DtoForRoleAndEmailInRequest() {}

    public DtoForRoleAndEmailInRequest(String email,String role){
        this.email=email;
        this.role=role;
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
}
