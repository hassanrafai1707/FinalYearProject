package com.FinalYearProject.FinalYearProject.DTO.UserDto;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
public class DtoFor2IDsAndPassword {
    Long replaceID,originalID;
    String password;

    public Long getReplaceID() {
        return replaceID;
    }

    public void setReplaceID(Long replaceID) {
        this.replaceID = replaceID;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Long getOriginalID() {
        return originalID;
    }

    public void setOriginalID(Long originalID) {
        this.originalID = originalID;
    }
}
