package com.FinalYearProject.FinalYearProject.DTO.QuestionDto;

import com.FinalYearProject.FinalYearProject.Domain.Question;
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

    public static QuestionDTO questionToQuestionDTO(Question question){
        return new QuestionDTO(
                question.getId(),
                question.getSubjectName(),
                question.getQuestionMarks(),
                question.getMappedCO(),
                question.getSubjectCode(),
                question.getCognitiveLevel(),
                question.getQuestionBody()
        );
    }

    public Long getId() {
            return Id;
        }

        public String getQuestionBody() {
            return questionBody;
        }

        public void setQuestionBody(String questionBody) {
            this.questionBody = questionBody;
        }

        public String getCognitiveLevel() {
            return cognitiveLevel;
        }

        public void setCognitiveLevel(String cognitiveLevel) {
            this.cognitiveLevel = cognitiveLevel;
        }

        public String getSubjectCode() {
            return subjectCode;
        }

        public void setSubjectCode(String subjectCode) {
            this.subjectCode = subjectCode;
        }

        public String getMappedCO() {
            return mappedCO;
        }

        public void setMappedCO(String mappedCO) {
            this.mappedCO = mappedCO;
        }

        public int getQuestionMarks() {
            return questionMarks;
        }

        public void setQuestionMarks(int questionMarks) {
            this.questionMarks = questionMarks;
        }

        public String getSubjectName() {
            return subjectName;
        }

        public void setSubjectName(String subjectName) {
            this.subjectName = subjectName;
        }

}
