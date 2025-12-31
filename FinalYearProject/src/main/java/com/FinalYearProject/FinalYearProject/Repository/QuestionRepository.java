package com.FinalYearProject.FinalYearProject.Repository;

import com.FinalYearProject.FinalYearProject.Domain.Question;
import com.FinalYearProject.FinalYearProject.Domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * QuestionPaperRepository - JPA Repository for Exam Paper Data Access
 * PURPOSE: Data access layer for QuestionPaper entities providing CRUD operations and custom queries for exam paper management and approval workflows.
 * REPOSITORY TYPE: Extends JpaRepository<QuestionPaper, Long> for standard CRUD operations with Long primary key. @Repository annotation enables Spring Data JPA scanning.
 * CUSTOM QUERIES: Uses @Query annotations for JPQL queries providing type-safe database operations beyond generated methods.
 * KEY QUERY METHODS: findByExamTitle (exact title match), findByGeneratedBy (papers created by specific user), findByApprovedBy (papers approved by specific supervisor), findByApproved (filter by approval status).
 * PAGINATION SUPPORT: Overloaded methods with Pageable parameter for paginated results. Supports efficient data retrieval for large paper collections.
 * APPROVAL WORKFLOW QUERIES: findByApproved supports filtering approved (true), not approved (false), and pending (null) papers. Critical for supervisor dashboard and approval workflows.
 * FINGERPRINT VALIDATION: existsByQuestionPaperFingerprint provides efficient duplicate detection using content-based fingerprinting. Ensures paper uniqueness.
 * PERFORMANCE OPTIMIZATION: JPQL queries leverage database indexes defined on QuestionPaper entity. Pagination reduces memory usage for large result sets.
 * INTEGRATION: Used by QuestionPaperService for all paper-related data operations. Supports teacher paper generation, supervisor approval, and student paper access.
 * QUERY DESIGN: Uses JPQL over native SQL for database portability. Parameter binding prevents SQL injection. Method names follow Spring Data naming conventions.
 */
@Repository
public interface QuestionRepository extends JpaRepository<Question , Long> {

    //simple Query handled by spring boot
    List<Question> findBySubjectCode(String subjectCode);
    Page<Question> findBySubjectCode(String subjectCode,Pageable pageable);
    List<Question> findBySubjectName(String subjectName);
    Page<Question> findBySubjectName(String subjectName,Pageable pageable);
    Boolean existsByQuestionTitle(String question);
    Optional<Question> findByQuestionTitle(String questionTitle);

    // complex query handled by @Query
    @Query("SELECT q FROM Question q WHERE q.subjectCode=:subjectCode AND q.mappedCO=:mappedCO")
    List<Question> findBySubjectCodeAndMappedCO(String subjectCode, String mappedCO);
    @Query("SELECT q FROM Question q WHERE q.subjectCode=:subjectCode AND q.mappedCO=:mappedCO")
    Page<Question> findBySubjectCodeAndMappedCO(String subjectCode, String mappedCO,Pageable pageable);
    @Query("SELECT q FROM Question q WHERE q.subjectCode=:subjectCode AND q.mappedCO=:mappedCO AND q.cognitiveLevel=:cognitiveLevel")
    List<Question> findBySubjectCodeAndMappedCOAndCognitiveLevel(String subjectCode, String mappedCO, String cognitiveLevel);
    @Query("SELECT q FROM Question q WHERE q.subjectCode=:subjectCode AND q.mappedCO=:mappedCO AND q.cognitiveLevel=:cognitiveLevel")
    Page<Question> findBySubjectCodeAndMappedCOAndCognitiveLevel(String subjectCode, String mappedCO, String cognitiveLevel,Pageable pageable);

    @Query("SELECT q FROM Question q WHERE q.subjectName=:subjectName AND q.mappedCO=:mappedCO")
    List<Question> findBySubjectNameAndMappedCO(String subjectName ,String mappedCO);
    @Query("SELECT q FROM Question q WHERE q.subjectName=:subjectName AND q.mappedCO=:mappedCO")
    Page<Question> findBySubjectNameAndMappedCO(String subjectName ,String mappedCO, Pageable pageable);
    @Query("SELECT q FROM Question q WHERE q.subjectName=:subjectName AND q.mappedCO=:mappedCO AND q.cognitiveLevel =:cognitiveLevel")
    List<Question> findBySubjectNameAndMappedCOAndCognitiveLevel(String subjectName ,String mappedCO ,String cognitiveLevel);
    @Query("SELECT q FROM Question q WHERE q.subjectName=:subjectName AND q.mappedCO=:mappedCO AND q.cognitiveLevel =:cognitiveLevel")
    List<Question> findBySubjectNameAndMappedCOAndCognitiveLevel(String subjectName ,String mappedCO ,String cognitiveLevel,Pageable pageable);

    @Query("SELECT q FROM Question q WHERE q.subjectCode=:subjectCode AND q.mappedCO IN :mappedCOs")
    List<Question> findValidQuestionsWithSubjectCode(String subjectCode,String[] mappedCOs);
    @Query("SELECT q FROM Question q WHERE q.subjectName=:subjectName AND q.mappedCO IN :mappedCOs")
    List<Question> findValidQuestionWithSubjectName(String subjectName,String[] mappedCOs);

    @Query("SELECT q FROM Question q WHERE q.createdBy =:user")
    List<Question> findByCreatedBy(User user);
    @Query("SELECT q FROM Question q WHERE q.createdBy =:user")
    Page<Question> findByCreatedBy(User user , Pageable pageable);
}
