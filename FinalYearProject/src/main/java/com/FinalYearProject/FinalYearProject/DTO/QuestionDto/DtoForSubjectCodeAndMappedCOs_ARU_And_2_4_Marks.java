package com.FinalYearProject.FinalYearProject.DTO.QuestionDto;

/**
 * DTO for Intelligent Question Paper Generation by Subject Code
 * PURPOSE: Complex Data Transfer Object that encapsulates all parameters for algorithm-driven exam paper generation. Enables automated creation of balanced question papers based on multiple constraints.
 * PAPER GENERATION PARAMETERS: subjectCode - target subject. mappedCOs[] - array of course outcomes to cover. numberOfCognitiveLevel_A/R/U - counts for Apply, Remember, Understand questions. maxNumberOf2Marks/4Marks - mark distribution limits.
 * COGNITIVE LEVEL BALANCING: Uses simplified Bloom's taxonomy (A=Apply, R=Remember, U=Understand) for cognitive diversity in generated papers. Supports differentiated assessment design.
 * OUTCOME COVERAGE: mappedCOs array allows multiple course outcomes to be covered in single paper. Ensures curriculum alignment and comprehensive assessment.
 * MARK DISTRIBUTION: Separate limits for 2-mark and 4-mark questions enables flexible paper structure design. Supports institutional marking scheme requirements.
 * ALGORITHM INPUT: This DTO provides complete specification for QuestionService paper generation algorithms. Used by generateBySubjectCodeQuestionPaper endpoint.
 * VALIDATION: Parameters should be validated for positive integers, non-empty arrays, and realistic constraints (e.g., total marks calculation).
 * EXTENSIBILITY: Could be extended with additional parameters: difficulty distribution, topic weighting, question type ratios, or time allocation constraints.
 * SERIALIZATION: Complex object suitable for JSON configuration of paper generation. Enables teachers to specify precise paper requirements through API.
 */
public class DtoForSubjectCodeAndMappedCOs_ARU_And_2_4_Marks {
    String subjectCode;
    String[] mappedCOs;
    int numberOfCognitiveLevel_A;
    int numberOfCognitiveLevel_R;
    int numberOfCognitiveLevel_U;
    int maxNumberOf2Marks;
    int maxNumberOf4Marks;

    public DtoForSubjectCodeAndMappedCOs_ARU_And_2_4_Marks() {}
    public DtoForSubjectCodeAndMappedCOs_ARU_And_2_4_Marks(
            String subjectCode,
            String[] mappedCOs,
            int numberOfCognitiveLevel_A,
            int numberOfCognitiveLevel_R,
            int numberOfCognitiveLevel_U ,
            int maxNumberOf2Marks,
            int maxNumberOf4Marks){
        this.subjectCode=subjectCode;
        this.mappedCOs=mappedCOs;
        this.numberOfCognitiveLevel_A = numberOfCognitiveLevel_A;
        this.numberOfCognitiveLevel_R=numberOfCognitiveLevel_R;
        this.numberOfCognitiveLevel_U =numberOfCognitiveLevel_U;
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

    public String getSubjectCode() {
        return subjectCode;
    }

    public void setSubjectCode(String subjectCode) {
        this.subjectCode = subjectCode;
    }
}
