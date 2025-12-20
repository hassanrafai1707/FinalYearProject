package com.FinalYearProject.FinalYearProject.Repository;

import com.FinalYearProject.FinalYearProject.Domain.QuestionPaper;
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
public interface QuestionPaperRepository extends JpaRepository<QuestionPaper,Long> {

    @Query("select q from QuestionPaper q where q.examTitle=:examTitle")
    public Optional<QuestionPaper> findByExamTitle(String examTitle);

    @Query("select q from QuestionPaper q where q.generatedBy=:generatedBy")
    public List<QuestionPaper> findByGeneratedBy(User generatedBy);

    @Query("select q from QuestionPaper q where q.generatedBy=:generatedBy")
    public Page<QuestionPaper> findByGeneratedBy(User generatedBy,Pageable pageable);

    @Query("SELECT q FROM QuestionPaper q WHERE q.approvedBy=:approvedBy")
    public List<QuestionPaper> findByApprovedBy(User approvedBy);

    @Query("SELECT q FROM QuestionPaper q WHERE q.approvedBy=:approvedBy")
    public Page<QuestionPaper> findByApprovedBy(User approvedBy ,Pageable pageable);

    @Query("SELECT q FROM QuestionPaper q WHERE q.approved=:approved")
    public List<QuestionPaper> findByApproved(Boolean approved);

    @Query("SELECT q FROM QuestionPaper q WHERE q.approved=:approved")
    public Page<QuestionPaper> findByApproved(Boolean approved,Pageable pageable);

    public Boolean existsByQuestionPaperFingerprint(String questionPaperFingerprint);

}
