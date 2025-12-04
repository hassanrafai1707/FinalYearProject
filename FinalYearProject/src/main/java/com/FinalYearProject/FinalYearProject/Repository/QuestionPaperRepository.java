package com.FinalYearProject.FinalYearProject.Repository;

import com.FinalYearProject.FinalYearProject.Domain.Question;
import com.FinalYearProject.FinalYearProject.Domain.QuestionPaper;
import com.FinalYearProject.FinalYearProject.Domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface QuestionPaperRepository extends JpaRepository<QuestionPaper,Long> {

    @Query("select q from QuestionPaper q where q.examTitle=:examTitle")
    public Optional<QuestionPaper> findByExamTitle(String examTitle);

    @Query("select q from QuestionPaper q where q.generatedBy=:generatedBy")
    public Optional<User> findByGeneratedBy(User user);

    @Query("SELECT qp FROM QuestionPaper qp JOIN qp.listOfQuestion q WHERE q =:question")
    public Optional<Question> findByListOfQuestionBy(Question question);

}
