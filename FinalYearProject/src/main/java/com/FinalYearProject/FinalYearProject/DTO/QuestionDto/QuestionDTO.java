package com.FinalYearProject.FinalYearProject.DTO.QuestionDto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class QuestionDTO {
        private Long Id;

        private String subjectName;

        private int questionMarks;

        private String mappedCO;

        private String subjectCode;

        private String cognitiveLevel;

        private String questionBody;

   }
