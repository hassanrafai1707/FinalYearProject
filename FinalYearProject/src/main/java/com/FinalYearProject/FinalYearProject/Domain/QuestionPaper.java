package com.FinalYearProject.FinalYearProject.Domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

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

    @ManyToMany(fetch = FetchType.LAZY,cascade = CascadeType.ALL)
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
