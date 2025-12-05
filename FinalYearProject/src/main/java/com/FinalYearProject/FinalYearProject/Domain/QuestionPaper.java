package com.FinalYearProject.FinalYearProject.Domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
//TODO Use indexing to reduce time complexity
@Table(name = "QuestionPaper")
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
    @JoinColumn(nullable = false,name = "User_Id")
    private User generatedBy;

    @ManyToOne
    @JoinColumn(nullable = false,name = "listOfQuestion" )
    private Question listOfQuestion;

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

    public Question getListOfQuestion() {
        return listOfQuestion;
    }

    public void setListOfQuestion(Question listOfQuestion) {
        this.listOfQuestion = listOfQuestion;
    }
}
