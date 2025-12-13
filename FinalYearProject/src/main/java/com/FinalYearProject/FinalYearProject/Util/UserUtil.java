package com.FinalYearProject.FinalYearProject.Util;

import com.FinalYearProject.FinalYearProject.Domain.UserPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;

public class UserUtil {
    private UserUtil(){}

    public static UserPrincipal getUserAuthentication(){
        return (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}
