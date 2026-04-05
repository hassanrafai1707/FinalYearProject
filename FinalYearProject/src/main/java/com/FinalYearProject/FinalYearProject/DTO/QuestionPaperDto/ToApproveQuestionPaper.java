package com.FinalYearProject.FinalYearProject.DTO.QuestionPaperDto;

import com.FinalYearProject.FinalYearProject.DTO.QuestionDto.QuestionDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ToApproveQuestionPaper {
    List<QuestionDTO> questionDTOList;
    String examTitle;
}
