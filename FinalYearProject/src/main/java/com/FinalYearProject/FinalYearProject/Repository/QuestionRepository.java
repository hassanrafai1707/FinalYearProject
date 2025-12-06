package com.FinalYearProject.FinalYearProject.Repository;

import com.FinalYearProject.FinalYearProject.Domain.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QuestionRepository extends JpaRepository<Question , Long> {

    public List<Question> findByMappedCO(String mappedCO);
    public List<Question> findBySubjectName(String subjectName);
    public List<Question> findBySubjectCode(String subjectCode);
    public List<Question> findByCognitiveLevel(String cognitiveLevel);
    public Boolean existsByQuestionTitle(String question);
    @Query("SELECT q FROM Question q WHERE q.questionTitle =:QuestionTitle")
    public Optional<Question> findByQuestionTitle(String questionTitle);
    @Query("SELECT q FROM Question q WHERE q.createdBy.email =:email")
    public List<Question> findByCreatedByUsingEmail(String email);
    @Query("SELECT q FROM Question q WHERE q.createdBy.id=:Id")
    public List<Question> findByCreatedByUsingId(Long Id);
}
