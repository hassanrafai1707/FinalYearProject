package com.FinalYearProject.FinalYearProject.Repository;

import com.FinalYearProject.FinalYearProject.Domain.QuestionPaper;
import com.FinalYearProject.FinalYearProject.Domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QuestionPaperRepository extends JpaRepository<QuestionPaper,Long> {

    @Query("select q from QuestionPaper q where q.examTitle=:examTitle")
    public Optional<QuestionPaper> findByExamTitle(String examTitle);

    @Query("select q from QuestionPaper q where q.generatedBy=:generatedBy")
    public List<QuestionPaper> findByGeneratedBy(User generatedBy);

    @Query("SELECT q FROM QuestionPaper q WHERE q.approvedBy=:approvedBy")
    public List<QuestionPaper> findByApprovedBy(User approvedBy);

    @Query("SELECT q FROM QuestionPaper q WHERE q.approved=:approved")
    public List<QuestionPaper> findByApproved(Boolean approved);

    public Boolean existsByQuestionPaperFingerprint(String questionPaperFingerprint);

}
