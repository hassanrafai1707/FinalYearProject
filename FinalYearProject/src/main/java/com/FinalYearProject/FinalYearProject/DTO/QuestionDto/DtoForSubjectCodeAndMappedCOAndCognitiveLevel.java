package com.FinalYearProject.FinalYearProject.DTO.QuestionDto;

public class DtoForSubjectCodeAndMappedCOAndCognitiveLevel {
    String subjectCode;
    String mappedCO;
    String cognitiveLevel;

    public DtoForSubjectCodeAndMappedCOAndCognitiveLevel(){}

    public DtoForSubjectCodeAndMappedCOAndCognitiveLevel(String subjectCode, String mappedCo, String cognitiveLevel){
        this.subjectCode=subjectCode;
        this.mappedCO =mappedCo;
        this.cognitiveLevel=cognitiveLevel;
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

    public String getCognitiveLevel() {
        return cognitiveLevel;
    }

    public void setCognitiveLevel(String cognitiveLevel) {
        this.cognitiveLevel = cognitiveLevel;
    }
}
