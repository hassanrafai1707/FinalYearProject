package com.FinalYearProject.FinalYearProject.DTO.QuestionDto;

public class DtoForSubjectCodeAndMappedCO {
    String mappedCO;
    String subjectCode;

    public DtoForSubjectCodeAndMappedCO(){}
    public DtoForSubjectCodeAndMappedCO(String mappedCO, String subjectCode){
        this.mappedCO=mappedCO;
        this.subjectCode=subjectCode;
    }

    public String getMappedCO() {
        return mappedCO;
    }

    public String getSubjectCode() {
        return subjectCode;
    }

    public void setMappedCO(String mappedCO) {
        this.mappedCO = mappedCO;
    }

    public void setSubjectCode(String subjectCode) {
        this.subjectCode = subjectCode;
    }
}
