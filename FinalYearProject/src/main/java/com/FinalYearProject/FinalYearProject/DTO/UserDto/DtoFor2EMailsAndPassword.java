package com.FinalYearProject.FinalYearProject.DTO.UserDto;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
public class DtoFor2EMailsAndPassword {
    private String replaceEmail , originalEmail, password;

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getOriginalEmail() {
        return originalEmail;
    }

    public void setOriginalEmail(String originalEmail) {
        this.originalEmail = originalEmail;
    }

    public String getReplaceEmail() {
        return replaceEmail;
    }

    public void setReplaceEmail(String replaceEmail) {
        this.replaceEmail = replaceEmail;
    }
}
