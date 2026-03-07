package com.FinalYearProject.FinalYearProject.Domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

/**
 * Question Domain Entity for Exam Question Bank Management
 * PURPOSE: Represents individual exam questions in the question bank with comprehensive metadata for curriculum alignment, cognitive level classification, and question paper generation.
 * DATABASE DESIGN: Uses @Entity for JPA persistence. @Table with optimized indexes for common query patterns. @SequenceGenerator for database-managed ID generation.
 * COMPREHENSIVE INDEXING STRATEGY:
 * - Simple indexes: id (primary), subject_name, subject_code, question_title, user_id (created_by)
 * - Compound indexes: subject_code+mapped_co, subject_code+mapped_co+cognitive_level, subject_name+mapped_co, subject_name+mapped_co+cognitive_level
 * - Enables efficient filtering for question paper generation algorithms
 * CURRICULUM ALIGNMENT FIELDS:
 * - subjectName & subjectCode: Course/subject identification
 * - mappedCO: Course Outcome mapping for outcome-based education
 * - cognitiveLevel: Bloom's taxonomy level (Remember, Understand, Apply, Analyze, Evaluate, Create)
 * QUESTION CONTENT FIELDS:
 * - questionBody: TEXT column for long question content
 * - questionTitle: Unique fingerprint/identifier for each question
 * - questionMarks: Marks allocated to this question
 * - isInUse: Flag indicating if question is currently in active papers
 * RELATIONSHIPS:
 * - ManyToOne to User (createdBy): Tracks question creator/author
 * - ManyToMany to QuestionPaper (questionPapers): Tracks which papers include this question (inverse side)
 * SECURITY & AUDIT: createdBy field provides audit trail of question authorship. isInUse prevents deletion of questions currently in active exams.
 * PERFORMANCE OPTIMIZATIONS: FetchType.LAZY for questionPapers relationship. Database-level indexing for all filter combinations used in paper generation.
 * USAGE IN PAPER GENERATION: Indexed fields enable efficient querying for balanced paper generation algorithms that consider subject, outcomes, cognitive levels, and marks distribution.
 */
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Entity
@Table(name="QuestionTable",indexes = {
        //simple index
        @Index(name = "index_Question_id",columnList = "id"),
        @Index(name = "index_subject_name",columnList = "subject_name"),
        @Index(name = "index_subject_code",columnList = "subject_code"),
        @Index(name = "index_question_title", columnList = "question_title"),
        @Index(name = "index_created_by" , columnList = "user_id"),
        // compound index
        @Index(name = "index_subject_code_and_mapped_co",
               columnList = "subject_code, mapped_co"
        ),
        @Index(name = "index_subject_code_and_mapped_co_and_cognitive_level",
               columnList = "subject_code, mapped_co, cognitive_level"
        ),
        @Index(name = "index_subject_name_and_mapped_co" ,
               columnList = "subject_name , mapped_co"
        ),
        @Index(name = "index_subject_name_and_mapped_co_and_cognitive_level",
               columnList = "subject_name, mapped_co, cognitive_level"
        )
})
public class Question {
    @Id
    @SequenceGenerator(
            name = "QuestionSequence",
            sequenceName = "QuestionSequence",
            allocationSize = 1
    )
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE ,
            generator = "QuestionSequence"
    )
    @Column(nullable = false,unique = true,updatable = false)
    private Long Id;

    @Column(nullable = false)
    private String subjectName;

    @Column(nullable = false)
    private int questionMarks;

    @Column(name = "mapped_co", nullable = false)
    private String mappedCO;

    @Column(nullable = false)
    private String subjectCode;

    @Column(nullable = false)
    private String cognitiveLevel;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String questionBody;

    @Column(nullable = false,unique = true)
    private String questionTitle; // this is a unique fingerprint for each question

    @Column(nullable = false)
    private Boolean isInUse;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false,name = "User_Id")
    private User createdBy;

    @ManyToMany(mappedBy = "listOfQuestion",fetch = FetchType.LAZY)
    private Set<QuestionPaper> questionPapers;

    public String getDepartment(){
        return createdBy.getDepartment();
    }
    public Set<QuestionPaper> getQuestionPapers() {
        return questionPapers;
    }

    public void setQuestionPapers(Set<QuestionPaper> questionPapers) {
        this.questionPapers = questionPapers;
    }

    public void setCreatedBy(User createdBy) {
        this.createdBy = createdBy;
    }

    public Long getId() {
        return Id;
    }

    public User getCreatedBy() {
        return createdBy;
    }

    public String getQuestionBody() {
        return questionBody;
    }

    public void setQuestionBody(String questionBody) {
        this.questionBody = questionBody;
    }

    public String getCognitiveLevel() {
        return cognitiveLevel;
    }

    public void setCognitiveLevel(String cognitiveLevel) {
        this.cognitiveLevel = cognitiveLevel;
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

    public int getQuestionMarks() {
        return questionMarks;
    }

    public void setQuestionMarks(int questionMarks) {
        this.questionMarks = questionMarks;
    }

    public String getSubjectName() {
        return subjectName;
    }

    public void setSubjectName(String subjectName) {
        this.subjectName = subjectName;
    }

    public Boolean getInUse() {
        return isInUse;
    }

    public void setInUse(Boolean inUse) {
        isInUse = inUse;
    }

    public String getQuestionTitle() {
        return questionTitle;
    }

    public void setQuestionTitle(String questionTitle) {
        this.questionTitle = questionTitle;
    }
}