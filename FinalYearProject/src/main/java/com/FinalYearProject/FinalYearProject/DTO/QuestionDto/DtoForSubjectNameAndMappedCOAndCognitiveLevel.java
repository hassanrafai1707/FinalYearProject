package com.FinalYearProject.FinalYearProject.DTO.QuestionDto;

/**
 * DTO for Subject Name, Mapped CO, and Cognitive Level Filtering
 * PURPOSE: Data Transfer Object for three-dimensional question filtering using subject name, course outcome, and cognitive level. Human-readable alternative to subject code based filtering.
 * FIELD DEFINITIONS: subjectName - descriptive subject title. mappedCO - Course Outcome identifier. cognitiveLevel - Bloom's taxonomy cognitive classification.
 * USAGE CONTEXT: Used in advanced search endpoints for users who prefer subject names over codes. Supports findBySubjectNameMappedCOCognitiveLevel controller methods.
 * COGNITIVE LEVEL INTEGRATION: Enables retrieval of questions at specific cognitive levels within subject name and outcome constraints. Supports learning objective targeting.
 * USER-CENTERED DESIGN: Provides intuitive filtering for educators who think in terms of subject names rather than institutional codes. Improves usability for non-technical users.
 * INTEGRATION: Works with QuestionService.findBySubjectNameMappedCOCognitiveLevel() method. Used by student, teacher, and supervisor controllers for precise question retrieval.
 * VALIDATION: Subject names should be validated against known course catalog. Cognitive levels should match predefined taxonomy values (A/R/U or full taxonomy names).
 * SERIALIZATION: Clean three-field POJO for JSON request bodies. Supports both manual construction and framework deserialization.
 * CONSISTENCY CONSIDERATIONS: Subject names must match exactly with Question entity values. Frontend should provide subject name suggestions to ensure valid queries.
 */
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
