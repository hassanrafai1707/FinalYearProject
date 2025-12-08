package com.FinalYearProject.FinalYearProject.DTO.QuestionDto;

public class DtoForSubjectNameAndMappedCO {
    String subjectName;
    String mappedCO;
    public DtoForSubjectNameAndMappedCO() {}

    public DtoForSubjectNameAndMappedCO(String subjectName, String mappedCO){
        this.subjectName = subjectName;
        this.mappedCO=mappedCO;
    }

    public String getSubjectName() {
        return subjectName;
    }

    public void setSubjectName(String subjectName) {
        this.subjectName = subjectName;
    }

    public String getMappedCO() {
        return mappedCO;
    }

    public void setMappedCO(String mappedCO) {
        this.mappedCO = mappedCO;
    }
}
