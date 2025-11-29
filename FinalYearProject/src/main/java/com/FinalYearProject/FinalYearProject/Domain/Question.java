package com.FinalYearProject.FinalYearProject.Domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Entity
@Table(name="QuestionTable")
public class Question {
    @Id
    @SequenceGenerator(
            name = "QuestionSequence",
            sequenceName = "QuestionSequence",
            allocationSize = 1
    )
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE ,
            generator = "QuestionSequence"
    )
    private Long Id;
    private String subjectName;
    private int questionMarks;
    private String mappedCO;
    private String subjectCode;
    private String cognitiveLevel;
    private String questionBody;
    private Boolean isInUse;
    @ManyToOne
    @JoinColumn(nullable = false,name = "User_Id")
    private User createdBy;

    public void setCreatedBy(User createdBy) {
        this.createdBy = createdBy;
    }

    public Long getId() {
        return Id;
    }

    public User getCreatedBy() {
        return createdBy;
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

    public Boolean getInUse() {
        return isInUse;
    }

    public void setInUse(Boolean inUse) {
        isInUse = inUse;
    }
}