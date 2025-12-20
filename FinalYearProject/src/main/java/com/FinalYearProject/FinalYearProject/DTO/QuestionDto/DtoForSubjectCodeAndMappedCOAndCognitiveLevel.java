package com.FinalYearProject.FinalYearProject.DTO.QuestionDto;

/**
 * DTO for Subject Code, Mapped CO, and Cognitive Level Filtering
 * PURPOSE: Data Transfer Object for three-dimensional question filtering by subject code, course outcome, and Bloom's taxonomy cognitive level. Enables precise question retrieval for curriculum-aligned assessments.
 * FIELD DEFINITIONS: subjectCode - academic subject identifier. mappedCO - Course Outcome code. cognitiveLevel - Bloom's taxonomy level (Remember, Understand, Apply, Analyze, Evaluate, Create).
 * USAGE CONTEXT: Used in advanced question search endpoints for students, teachers, and supervisors. Supports outcome-based education with cognitive level differentiation.
 * COGNITIVE LEVEL TAXONOMY: Enables retrieval of questions at specific Bloom's taxonomy levels, supporting differentiated assessment design and learning objective targeting.
 * INTEGRATION: Used with QuestionService.findBySubjectCodeMappedCOCognitiveLevel() method. Supports both list and paginated response formats.
 * VALIDATION: Cognitive level should be validated against predefined enum/constants (A/R/U or full taxonomy names). Ensures consistency in question classification.
 * EXTENSION VALUE: Triple-filter combination enables highly targeted question retrieval for creating balanced exam papers with specific cognitive level distributions.
 * SERIALIZATION: Simple POJO with getters/setters for JSON serialization. Default constructor for framework deserialization. Parameterized constructor for programmatic creation.
 */
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
