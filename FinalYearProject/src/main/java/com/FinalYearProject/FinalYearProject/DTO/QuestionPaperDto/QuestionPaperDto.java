package com.FinalYearProject.FinalYearProject.DTO.QuestionPaperDto;

import com.FinalYearProject.FinalYearProject.DTO.QuestionDto.QuestionDTO;
import com.FinalYearProject.FinalYearProject.DTO.UserDto.UserDto;
import com.FinalYearProject.FinalYearProject.Domain.Question;
import com.FinalYearProject.FinalYearProject.Domain.User;
import com.FinalYearProject.FinalYearProject.Util.QuestionDtoUtil;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@NoArgsConstructor
@Getter
@Setter
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
                QuestionDtoUtil.listOfQuestionToQuestionDto(
                listOfQuestion.stream().toList()
                )
        );
    }

    public Set<QuestionDTO> getListOfQuestion() {
        return listOfQuestion;
    }

    public void setListOfQuestion(Set<QuestionDTO> listOfQuestion) {
        this.listOfQuestion = listOfQuestion;
    }

    public UserDto getApprovedBy() {
        return approvedBy;
    }

    public void setApprovedBy(User approvedBy) {
        this.approvedBy = helper(approvedBy);
    }

    public Boolean getApproved() {
        return approved;
    }

    public void setApproved(Boolean approved) {
        this.approved = approved;
    }

    public UserDto getGeneratedBy() {
        return generatedBy;
    }

    public void setGeneratedBy(User generatedBy) {
        this.generatedBy = helper(generatedBy);
    }

    public String getExamTitle() {
        return examTitle;
    }

    public void setExamTitle(String examTitle) {
        this.examTitle = examTitle;
    }

    public Long getId() {
        return Id;
    }

    public void setId(Long id) {
        Id = id;
    }

    public static UserDto helper(User user){
        if (user!=null){
            return new UserDto(
                    user.getId(),
                    user.getName(),
                    user.getEmail(),
                    user.getRole(),
                    user.isIs_enable(),
                    user.isLocked(),
                    user.isExpired()
            );
        }
        return new UserDto();
    }
}
