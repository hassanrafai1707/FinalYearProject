package com.FinalYearProject.FinalYearProject.Repository;

import com.FinalYearProject.FinalYearProject.Domain.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QuestionRepository extends JpaRepository<Question , Long> {

    public Optional<List<Question>> findByMappedCO(String mappedCO);
    public Optional<List<Question>> findBySubjectName(String subjectName);
    public Optional<List<Question>> findBySubjectCode(String subjectCode);
    public Optional<List<Question>> findByCognitiveLevel(String cognitiveLevel);
    public Boolean existsByQuestionBody(String question);
    @Query("SELECT q FROM Question q WHERE q.questionBody =:questionBody")
    public Optional<Question> findByQuestionBody(String questionBody);
    @Query("SELECT q FROM Question q WHERE q.createdBy.email =:email")
    public Optional<List<Question>> findByCreatedByUsingEmail(String email);
    @Query("SELECT q FROM Question q WHERE q.createdBy.id=:Id")
    public Optional<List<Question>> findByCreatedByUsingId(Long Id);
}
