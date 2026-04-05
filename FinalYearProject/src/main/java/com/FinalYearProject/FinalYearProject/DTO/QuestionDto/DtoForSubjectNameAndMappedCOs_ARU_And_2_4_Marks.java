package com.FinalYearProject.FinalYearProject.DTO.QuestionDto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for Intelligent Question Paper Generation by Subject Name
 * PURPOSE: Comprehensive Data Transfer Object for algorithm-driven exam paper generation using subject names. Encapsulates all constraints for creating balanced, curriculum-aligned question papers.
 * PAPER GENERATION SPECIFICATION: subjectName - target subject. mappedCOs[] - course outcomes array. numberOfCognitiveLevel_A/R/U - cognitive level distribution. maxNumberOf2Marks/4Marks - marking scheme limits.
 * SUBJECT NAME APPROACH: Uses human-readable subject names instead of codes, making it more intuitive for educators. Requires exact name matching with question bank data.
 * COGNITIVE BALANCE: Specifies exact counts for Apply (A), Remember (R), and Understand (U) questions. Enables creation of papers with controlled cognitive difficulty distribution.
 * OUTCOME COVERAGE: mappedCOs array allows multiple course outcomes to be assessed in single paper. Ensures comprehensive curriculum coverage and learning outcome validation.
 * MARKING SCHEME FLEXIBILITY: Separate limits for 2-mark and 4-mark questions supports various exam formats and institutional requirements.
 * ALGORITHM INPUT: Provides complete configuration for QuestionService.generateBySubjectNameQuestion() algorithm. Used by generateBySubjectNameAndQuestionPaper endpoint.
 * VALIDATION REQUIREMENTS: Parameters should be validated for positive integers, non-empty arrays, and realistic constraints. Subject name must exist in question bank.
 * EXTENSION POTENTIAL: Could be enhanced with additional parameters: time limits, question type mix, difficulty curve, or topic weightings for more sophisticated paper generation.
 * USER EXPERIENCE: Enables teachers to specify paper requirements in intuitive terms (subject name, cognitive levels, marks) rather than technical database queries.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DtoForSubjectNameAndMappedCOs_ARU_And_2_4_Marks {
    String subjectName;
    String[] mappedCOs;
    int numberOfCognitiveLevel_A;
    int numberOfCognitiveLevel_R;
    int numberOfCognitiveLevel_U;
    int maxNumberOf2Marks;
    int maxNumberOf4Marks;
}
