package com.FinalYearProject.FinalYearProject.DTO.QuestionPaperDto;

import com.FinalYearProject.FinalYearProject.DTO.QuestionDto.QuestionDTO;
import com.FinalYearProject.FinalYearProject.DTO.UserDto.UserDto;
import com.FinalYearProject.FinalYearProject.Domain.Question;
import com.FinalYearProject.FinalYearProject.Domain.User;
import com.FinalYearProject.FinalYearProject.Util.QuestionDtoUtil;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
public class QuestionPaperDto {
    private Long Id;

    private String examTitle;

    private UserDto generatedBy;

    private Boolean approved;

    private UserDto approvedBy;

    private String comment;

    private Set<QuestionDTO> listOfQuestion;


    public QuestionPaperDto(
            Long Id,
            String examTitle,
            User generatedBy,
            Boolean approved,
            User approvedBy,
            String comment,
            Set<Question> listOfQuestion
    ){
        this.Id=Id;
        this.examTitle=examTitle;
        this.generatedBy=helper(generatedBy);
        this.approved=approved;
        this.approvedBy=helper(approvedBy);
        this.comment=comment;
        this.listOfQuestion= new HashSet<>(//need to changer the type to question dto to send the info to user ( the reason question dto doesn't have question papers so it can be used )
                QuestionDtoUtil.listOfQuestionToQuestionDto(listOfQuestion.stream().toList()
                ));

    }

    public static UserDto helper(User user){
        if (user!=null){
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
        return new UserDto();
    }
}
