package com.FinalYearProject.FinalYearProject.DTO.QuestionDto;

public class DtoForSubjectNameAndMappedCOs_ARU_And_2_4_Marks {
    String subjectName;
    String[] mappedCOs;
    int numberOfCognitiveLevel_A;
    int numberOfCognitiveLevel_R;
    int numberOfCognitiveLevel_U;
    int maxNumberOf2Marks;
    int maxNumberOf4Marks;

    public DtoForSubjectNameAndMappedCOs_ARU_And_2_4_Marks() {}

    public DtoForSubjectNameAndMappedCOs_ARU_And_2_4_Marks(
            String subjectName,
            String[] mappedCOs,
            int numberOfCognitiveLevel_U,
            int numberOfCognitiveLevel_R,
            int numberOfCognitiveLevel_A,
            int maxNumberOf2Marks,
            int maxNumberOf4Marks
    ){
        this.subjectName=subjectName;
        this.mappedCOs=mappedCOs;
        this.numberOfCognitiveLevel_A=numberOfCognitiveLevel_A;
        this.numberOfCognitiveLevel_R=numberOfCognitiveLevel_R;
        this.numberOfCognitiveLevel_U=numberOfCognitiveLevel_U;
        this.maxNumberOf2Marks=maxNumberOf2Marks;
        this.maxNumberOf4Marks=maxNumberOf4Marks;
    }

    public int getMaxNumberOf4Marks() {
        return maxNumberOf4Marks;
    }

    public void setMaxNumberOf4Marks(int maxNumberOf4Marks) {
        this.maxNumberOf4Marks = maxNumberOf4Marks;
    }

    public int getMaxNumberOf2Marks() {
        return maxNumberOf2Marks;
    }

    public void setMaxNumberOf2Marks(int maxNumberOf2Marks) {
        this.maxNumberOf2Marks = maxNumberOf2Marks;
    }

    public int getNumberOfCognitiveLevel_U() {
        return numberOfCognitiveLevel_U;
    }

    public void setNumberOfCognitiveLevel_U(int numberOfCognitiveLevel_U) {
        this.numberOfCognitiveLevel_U = numberOfCognitiveLevel_U;
    }

    public int getNumberOfCognitiveLevel_R() {
        return numberOfCognitiveLevel_R;
    }

    public void setNumberOfCognitiveLevel_R(int numberOfCognitiveLevel_R) {
        this.numberOfCognitiveLevel_R = numberOfCognitiveLevel_R;
    }

    public int getNumberOfCognitiveLevel_A() {
        return numberOfCognitiveLevel_A;
    }

    public void setNumberOfCognitiveLevel_A(int numberOfCognitiveLevel_A) {
        this.numberOfCognitiveLevel_A = numberOfCognitiveLevel_A;
    }

    public String[] getMappedCOs() {
        return mappedCOs;
    }

    public void setMappedCOs(String[] mappedCOs) {
        this.mappedCOs = mappedCOs;
    }

    public String getSubjectName() {
        return subjectName;
    }

    public void setSubjectName(String subjectName) {
        this.subjectName = subjectName;
    }
}
