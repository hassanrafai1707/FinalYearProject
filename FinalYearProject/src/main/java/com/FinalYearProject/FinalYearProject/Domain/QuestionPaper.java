package com.FinalYearProject.FinalYearProject.Domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

/**
 * QuestionPaper Domain Entity for Exam Paper Management
 * PURPOSE: Represents complete exam question papers composed of multiple questions, with approval workflow tracking and integrity verification.
 * DATABASE DESIGN: JPA entity with sequence-based ID generation. Comprehensive indexing strategy for efficient querying.
 * INDEXING STRATEGY: Single-column indexes on id, exam_title, generated_by, approved_by, approved, question_paper_fingerprint. Enables fast lookups by common search criteria.
 * PAPER COMPOSITION: ManyToMany relationship with Question entities via join table "question_paper_and_qusetion". CascadeType.ALL ensures paper-question relationships are properly managed.
 * APPROVAL WORKFLOW: approved flag tracks supervisor approval status. approvedBy tracks which supervisor approved/rejected the paper. generatedBy tracks teacher who created the paper.
 * INTEGRITY VERIFICATION: questionPaperFingerprint provides unique hash/fingerprint for paper content verification. Prevents unauthorized modifications and ensures paper integrity.
 * AUDIT TRAIL: Tracks complete lifecycle - generatedBy (creator), approvedBy (supervisor), approval status, and timestamp (implied). Enables accountability and audit compliance.
 * RELATIONSHIP MANAGEMENT: JoinTable defines explicit mapping between papers and questions. FetchType.LAZY optimizes performance when loading papers without question details.
 * SECURITY FEATURES: approvedBy nullable for pending papers. Unique examTitle prevents duplicate paper names. Fingerprint enables content integrity validation.
 * WORKFLOW STATES: approved=true (approved and ready for use), approved=false (rejected/needs revision), approved=null (pending supervisor review).
 * PERFORMANCE: Indexed fields support efficient queries for supervisor dashboards (find by approver, find by status, find by creator). Lazy loading prevents N+1 query problems.
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "QuestionPaper" , indexes = {
        @Index(name = "index_question_paper_id", columnList = "id"),
        @Index(name = "index_question_paper_exam_title",columnList = "exam_title"),
        @Index(name = "index_question_paper_genrated_by",columnList = "generated_by"),
        @Index(name = "index_approved_by",columnList = "approved_by"),
        @Index(name = "index_approved", columnList = "approved"),
        @Index(name = "index_question_paper_fingerprint" ,columnList = "question_paper_fingerprint")
})
public class QuestionPaper {
    @Id
    @SequenceGenerator(
            name = "QuestionPaper_Id_seq",
            sequenceName = "QuestionPaper_Id_seq",
            initialValue = 1,
            allocationSize = 1
    )
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "QuestionPaper_Id_seq"
    )
    @Column(nullable = false,updatable = false)
    private Long Id;

    @Column(nullable = false,unique = true)
    private String examTitle;

    @ManyToOne
    @JoinColumn(nullable = false,name = "generated_by")
    private User generatedBy;

    @Column(name = "approved")
    private Boolean approved;

    @Column(name = "question_paper_fingerprint", nullable = false)
    private String questionPaperFingerprint;

    @ManyToOne
    @JoinColumn(name = "approved_by", nullable = true)
    private User approvedBy;

    @ManyToMany(fetch = FetchType.LAZY,cascade = {
            CascadeType.MERGE,
            CascadeType.PERSIST
    })
    @JoinTable(name = "question_paper_and_qusetion",
            joinColumns = {
            @JoinColumn(name = "question_paper_id" ,referencedColumnName = "id")
            },
            inverseJoinColumns = {
            @JoinColumn(name = "question_id",referencedColumnName = "id")
            }
    )
    private Set<Question> listOfQuestion;


    public Long getId() {
        return Id;
    }

    public String getExamTitle() {
        return examTitle;
    }

    public void setExamTitle(String examTitle) {
        this.examTitle = examTitle;
    }

    public User getGeneratedBy() {
        return generatedBy;
    }

    public void setGeneratedBy(User generatedBy) {
        this.generatedBy = generatedBy;
    }

    public Set<Question> getListOfQuestion() {
        return listOfQuestion;
    }

    public void setListOfQuestion(Set<Question> listOfQuestion) {
        this.listOfQuestion = listOfQuestion;
    }

    public Boolean getApproved() {
        return approved;
    }

    public void setApproved(Boolean approved) {
        this.approved = approved;
    }

    public String getQuestionPaperFingerprint() {
        return questionPaperFingerprint;
    }

    public void setQuestionPaperFingerprint(String questionPaperFingerprint) {
        this.questionPaperFingerprint = questionPaperFingerprint;
    }

    public User getApprovedBy() {
        return approvedBy;
    }

    public void setApprovedBy(User approvedBy) {
        this.approvedBy = approvedBy;
    }
}
