package com.FinalYearProject.FinalYearProject.DTO.QuestionDto;

/**
 * DTO for Subject Code and Mapped Course Outcome Filtering
 * PURPOSE: Data Transfer Object for filtering questions by subject code and mapped course outcome. Used in question bank search and paper generation endpoints.
 * FIELD DEFINITIONS: subjectCode - unique identifier for academic subject/course (e.g., "CS101"). mappedCO - Course Outcome code that question maps to (e.g., "CO1", "CO2").
 * USAGE CONTEXT: Primarily used in student and teacher controllers for filtered question retrieval. Supports findBySubjectCodeMappedCO endpoints with pagination variants.
 * INTEGRATION: Used with QuestionService methods that query questions by subject code and course outcome combination. Enables curriculum-aligned question searching.
 * SERIALIZATION: Simple POJO structure suitable for JSON request/response bodies. Default constructor for framework deserialization, parameterized constructor for manual creation.
 * VALIDATION: Field validation should occur at service layer (non-null, non-empty, valid format). Subject codes should follow institutional naming conventions.
 * EXTENSION POINT: Can be extended with additional filtering criteria (cognitive level, marks range, difficulty level) as needed for advanced search capabilities.
 */
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
