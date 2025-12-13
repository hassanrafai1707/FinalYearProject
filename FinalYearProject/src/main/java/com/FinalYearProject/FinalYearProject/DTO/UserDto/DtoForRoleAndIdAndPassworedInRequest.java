package com.FinalYearProject.FinalYearProject.DTO.UserDto;

public class DtoForRoleAndIdAndPassworedInRequest {
    Long id;
    String role;
    String password;

    public DtoForRoleAndIdAndPassworedInRequest() {}

    public DtoForRoleAndIdAndPassworedInRequest(Long id, String role,String password){
        this.role=role;
        this.id=id;
        this.password=password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
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
}
