package com.FinalYearProject.FinalYearProject.Repository;

import com.FinalYearProject.FinalYearProject.Domain.Question;
import com.FinalYearProject.FinalYearProject.Domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionRepository extends JpaRepository<Question , Long> {

    public List<Question> findByMappedCO(String mappedCO);
    public List<Question> findBySubjectName(String subjectName);
    public List<Question> findBySubjectCode(String subjectCode);
    public List<Question> findByCognitiveLevel(String cognitiveLevel);
    public List<Question> findByCreatedBy(User user);
    @Query("SELECT q FROM Question q WHERE q.id=(SELECT MIN(qs.id) FROM Question qs)")
    public User getSmallestId();
    @Query("SELECT q FROM Question q WHERE q.id=(SELECT MAX(qs.id) FROM Question qs)")
    public User getLargestId();
}
