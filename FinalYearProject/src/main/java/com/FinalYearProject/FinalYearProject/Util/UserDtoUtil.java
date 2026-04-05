package com.FinalYearProject.FinalYearProject.Util;

import com.FinalYearProject.FinalYearProject.DTO.UserDto.UserDto;
import com.FinalYearProject.FinalYearProject.Domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.stream.Collectors;

public class UserDtoUtil {

    private UserDtoUtil(){}

    public static UserDto UserToUserDto(User user){
        return new UserDto(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                user.is_enable(),
                user.isLocked(),
                user.isExpired()
        );
    }

    public static List<UserDto> listOfUserToUserDto(List<User> users){
        if (users.isEmpty()){
            throw new IllegalArgumentException("the user passed in this method can not be null");
        }
        return users.stream().map(user -> new UserDto(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                user.is_enable(),
                user.isLocked(),
                user.isExpired()
        )).collect(Collectors.toList());
    }

    public static Page<UserDto> UserToUserDtoPaged(Page<User> users){
        if (users.isEmpty()||users.getContent().isEmpty()){
            throw new IllegalArgumentException("the user passed in this method can not be null");
        }
        return users.map(
                user ->new UserDto(
                        user.getId(),
                        user.getName(),
                        user.getEmail(),
                        user.getRole(),
                        user.is_enable(),
                        user.isLocked(),
                        user.isExpired()
                ));
    }
}
