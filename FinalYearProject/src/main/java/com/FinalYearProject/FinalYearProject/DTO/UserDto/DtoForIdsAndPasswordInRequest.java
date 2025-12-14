package com.FinalYearProject.FinalYearProject.DTO.UserDto;

import java.util.ArrayList;
import java.util.List;

public class DtoForIdsAndPasswordInRequest {
    List<Long> ids=new ArrayList<>();
    String adminPassword;

    public DtoForIdsAndPasswordInRequest(String adminPassword, List<Long> ids){
        this.adminPassword=adminPassword;
        this.ids=ids;
    }

    public List<Long> getIds() {
        return ids;
    }

    public void setIds(List<Long> ids) {
        this.ids = ids;
    }

    public String getAdminPassword() {
        return adminPassword;
    }

    public void setAdminPassword(String adminPassword) {
        this.adminPassword = adminPassword;
    }
}
