package com.FinalYearProject.FinalYearProject.Repository;

import com.FinalYearProject.FinalYearProject.Domain.Question;
import com.FinalYearProject.FinalYearProject.Domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QuestionRepository extends JpaRepository<Question , Long> {

    //simple Query handled by spring boot
    List<Question> findBySubjectCode(String subjectCode);
    Page<Question> findBySubjectCode(String subjectCode,Pageable pageable);
    List<Question> findBySubjectName(String subjectName);
    Page<Question> findBySubjectName(String subjectName,Pageable pageable);
    Boolean existsByQuestionTitle(String question);
    Optional<Question> findByQuestionTitle(String questionTitle);

    // complex query handled by @Query
    @Query("SELECT q FROM Question q WHERE q.subjectCode=:subjectCode AND q.mappedCO=:mappedCO")
    List<Question> findBySubjectCodeAndMappedCO(String subjectCode, String mappedCO);
    @Query("SELECT q FROM Question q WHERE q.subjectCode=:subjectCode AND q.mappedCO=:mappedCO AND q.cognitiveLevel=:cognitiveLevel")
    List<Question> findBySubjectCodeAndMappedCOAndCognitiveLevel(String subjectCode, String mappedCO, String cognitiveLevel);

    @Query("SELECT q FROM Question q WHERE q.subjectName=:subjectName AND q.mappedCO=:mappedCO")
    List<Question> findBySubjectNameAndMappedCO(String subjectName ,String mappedCO);
    @Query("SELECT q FROM Question q WHERE q.subjectName=:subjectName AND q.mappedCO=:mappedCO AND q.cognitiveLevel =:cognitiveLevel")
    List<Question> findBySubjectNameAndMappedCOAndCognitiveLevel(String subjectName ,String mappedCO ,String cognitiveLevel);

    @Query("SELECT q FROM Question q WHERE q.subjectCode=:subjectCode AND q.mappedCO IN :mappedCOs")
    List<Question> findValidQuestionsWithSubjectCode(String subjectCode,String[] mappedCOs);
    @Query("SELECT q FROM Question q WHERE q.subjectName=:subjectName AND q.mappedCO IN : mappedCOs")
    List<Question> findValidQuestionWithSubjectName(String subjectName,String[] mappedCOs);

    @Query("SELECT q FROM Question q WHERE q.createdBy =:user")
    List<Question> findByCreatedBy(User user);

    @Query("SELECT q FROM Question q WHERE q.createdBy =:user")
    Page<Question> findByCreatedBy(User user , Pageable pageable);
}
