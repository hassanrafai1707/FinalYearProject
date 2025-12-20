package com.FinalYearProject.FinalYearProject.DTO.QuestionDto;

/**
 * DTO for Subject Name and Mapped Course Outcome Filtering
 * PURPOSE: Data Transfer Object for filtering questions by human-readable subject name and mapped course outcome. Alternative to subject code filtering using descriptive subject names.
 * FIELD DEFINITIONS: subjectName - descriptive name of academic subject (e.g., "Introduction to Programming"). mappedCO - Course Outcome code that question aligns with.
 * USAGE CONTEXT: Used in question bank search endpoints where users prefer subject names over codes. Supports findBySubjectNameMappedCO endpoints with pagination variants.
 * USER EXPERIENCE: Provides more intuitive filtering for end-users who may not know subject codes but recognize subject names. Complementary to subject code-based filtering.
 * INTEGRATION: Used with QuestionService.findBySubjectNameMappedCO() method. Supports both list responses and paginated results for large question banks.
 * VALIDATION: Subject names should match institutional course catalog. Course outcome codes should follow institutional formatting (CO1, CO2, etc.).
 * SERIALIZATION: Simple two-field POJO for clean JSON request bodies. Default constructor for Spring MVC binding. Parameterized constructor for programmatic use.
 * CONSISTENCY: Should maintain consistency with subject name values in Question entity. Consider dropdown/autocomplete for subject names in frontend to ensure valid values.
 */
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
