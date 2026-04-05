package com.FinalYearProject.FinalYearProject.DTO.UserDto;


import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDto {
    private Long Id;

    private String name;

    private String email;

    private String role;

    private boolean is_enable;

    private boolean locked;

    private boolean expired;
}
