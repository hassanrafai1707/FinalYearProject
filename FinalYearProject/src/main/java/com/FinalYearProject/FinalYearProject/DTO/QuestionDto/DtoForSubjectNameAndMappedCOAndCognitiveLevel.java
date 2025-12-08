package com.FinalYearProject.FinalYearProject.DTO.QuestionDto;

public class DtoForSubjectNameAndMappedCOAndCognitiveLevel {
    String subjectName;
    String mappedCO;
    String cognitiveLevel;

    public DtoForSubjectNameAndMappedCOAndCognitiveLevel() {}

    public DtoForSubjectNameAndMappedCOAndCognitiveLevel(String subjectName ,String mappedCO, String cognitiveLevel) {
        this.subjectName=subjectName;
        this.mappedCO = mappedCO;
        this.cognitiveLevel=cognitiveLevel;
    }

    public String getCognitiveLevel() {
        return cognitiveLevel;
    }

    public void setCognitiveLevel(String cognitiveLevel) {
        this.cognitiveLevel = cognitiveLevel;
    }

    public String getMappedCO() {
        return mappedCO;
    }

    public void setMappedCO(String mappedCO) {
        this.mappedCO = mappedCO;
    }

    public String getSubjectName() {
        return subjectName;
    }

    public void setSubjectName(String subjectName) {
        this.subjectName = subjectName;
    }
}
