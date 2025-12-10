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
        @Index(name = "index_question_paper_exam_title_id",columnList = "exam_title"),
        @Index(name = "index_question_paper_genrated_by",columnList = "generated_by"),
        @Index(name = "index_approved_by",columnList = "approved_by"),
        @Index(name = "index_approved", columnList = "approved")
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

    @Column(nullable = false)
    private String examTitle;

    @ManyToOne
    @JoinColumn(nullable = false,name = "generated_by")
    private User generatedBy;

    @Column(name = "approved")
    private Boolean approved;

    @ManyToOne
    @JoinColumn(name = "approved_by",nullable = false)
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
}
