package com.FinalYearProject.FinalYearProject.DTO.UserDto;

public class DtoForRoleAndIdInRequest {
    Long id;
    String role;

    public DtoForRoleAndIdInRequest() {}

    public DtoForRoleAndIdInRequest(Long id, String role){
        this.role=role;
        this.id=id;
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
}
