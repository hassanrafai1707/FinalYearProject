package com.FinalYearProject.FinalYearProject.DTO.UserDto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class DtoFor2IDsAndPassword {
    Long replaceID,originalID;
    String password;
}
