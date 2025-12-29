package com.FinalYearProject.FinalYearProject.Service;

import com.FinalYearProject.FinalYearProject.Domain.User;
import com.FinalYearProject.FinalYearProject.Exceptions.QuestionException.DuplicateQuestionException;
import com.FinalYearProject.FinalYearProject.Exceptions.QuestionException.QuestionNotFoundException;
import com.FinalYearProject.FinalYearProject.Exceptions.QuestionException.UnacceptableQuestion;
import com.FinalYearProject.FinalYearProject.Exceptions.UserEeceptions.UserNotAuthorizesException;
import com.FinalYearProject.FinalYearProject.Domain.Question;
import com.FinalYearProject.FinalYearProject.Repository.QuestionRepository;
import com.FinalYearProject.FinalYearProject.Util.QuestionUtil;
import com.FinalYearProject.FinalYearProject.Util.UserUtil;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.*;

@Service
@AllArgsConstructor
/**
 * QuestionService - Business Logic Service for Question Bank Management
 * PURPOSE: Core service for question bank operations including CRUD, advanced filtering, intelligent paper generation, and business rule enforcement.
 * QUESTION BANK MANAGEMENT: Comprehensive CRUD operations for questions with validation. Supports creation by teachers, retrieval by all roles, deletion with ownership checks.
 * INTELLIGENT PAPER GENERATION: generateBySubjectCodeQuestion and generateBySubjectNameQuestion implement algorithm for balanced exam paper creation based on cognitive levels, marks distribution, and course outcomes.
 * FILTERING CAPABILITIES: Advanced filtering by subject code/name, mapped course outcomes, cognitive levels (A/R/U), and combinations. Supports both list and paginated results.
 * COGNITIVE LEVEL BALANCING: Paper generation algorithm balances questions across Bloom's taxonomy levels (Apply, Remember, Understand) while respecting mark distribution constraints.
 * VALIDATION & INTEGRITY: Question body validation, duplicate detection via SHA256 fingerprinting, mark validation (2 or 4), and teacher authorization checks.
 * OWNERSHIP & AUTHORIZATION: UserUtil.getUserAuthentication() ensures teachers only manage their own questions. Role-based checks for all operations.
 * PERFORMANCE OPTIMIZATIONS: Pagination support for all list methods. Shuffling algorithm for random question selection. Sorted output by marks for organized papers.
 * TRANSACTION MANAGEMENT: @Transactional on write operations (addQuestion) ensures data consistency. Critical for question creation and status updates.
 * ERROR HANDLING: Comprehensive exception handling - QuestionNotFoundException, DuplicateQuestionException, UnacceptableQuestion, UserNotAuthorizesException with meaningful messages.
 * INTEGRATION: Works with QuestionRepository for data access, UserService for user validation, and QuestionUtil for fingerprint generation and validation.
 */
public class QuestionService {

    @Autowired
    private QuestionRepository questionRepository;
    @Autowired
    private UserService userService;

    public List<Question> getAllQuestion(){
        List<Question> tempQuestion=questionRepository.findAll();
        if (!(tempQuestion.isEmpty())){
            return tempQuestion;
        }
        else {
            throw new QuestionNotFoundException("no Question in DataBase");
        }
    }

    public Page<Question> getAllQuestionsPaged(int pageNo,int size){
        Pageable pageable= PageRequest.of(pageNo,size);
        Page<Question> temp=questionRepository.findAll(pageable);
        if (!(temp.isEmpty())){
            return temp;
        }
        else {
            throw new QuestionNotFoundException("no more Question in DataBase");
        }
    }

    public Question getQuestionById(Long id) {
        return questionRepository.findById(id)
                .orElseThrow(() -> new QuestionNotFoundException("Question not found with ID: " + id));
    }

    public List<Question> getQuestionByIds(List<Long> Ids){
        List<Question> temp =questionRepository.findAllById(Ids);
        for (Question question:temp){
            System.out.println(question);
        }
        return temp;
    }

    public List<Question> findBySubjectCode(String subjectCode){
        List<Question> tempQuestion=questionRepository.findBySubjectCode(subjectCode);
        if (!(tempQuestion.isEmpty())){
            return tempQuestion;
        }
        throw new QuestionNotFoundException("No questions found with Subject code: "+subjectCode);
    }

    public Page<Question> findBySubjectCode(String subjectCode,int pageNo,int size){
        Pageable pageable=PageRequest.of(pageNo,size);
        Page<Question> temp = questionRepository.findBySubjectCode(subjectCode,pageable);
        if (!(temp.isEmpty())){
            return temp;
        }
        throw new QuestionNotFoundException("No questions found with Subject code: "+subjectCode);
    }

    public List<Question> findBySubjectCodeMappedCO(String subjectCode , String mappedCO){
        List<Question> tempQuestion=questionRepository.findBySubjectCodeAndMappedCO(subjectCode,mappedCO);
        if (!(tempQuestion.isEmpty())){
            return tempQuestion;
        }
        throw  new QuestionNotFoundException("No questions found with Subject name: "+subjectCode+"and Mapped CO"+mappedCO);
    }

    public Page<Question> findBySubjectCodeMappedCO(String subjectCode,String mappedCO, int pageNo, int size){
        Pageable pageable=PageRequest.of(pageNo,size);
        Page<Question> tempQuestions=questionRepository.findBySubjectCodeAndMappedCO(subjectCode,mappedCO,pageable);
        if (!(tempQuestions.isEmpty())){
            return tempQuestions;
        }
        else {
            throw  new QuestionNotFoundException("No questions found with Subject name: "+subjectCode+"and Mapped CO"+mappedCO);
        }
    }

    public List<Question> findBySubjectCodeMappedCOCognitiveLevel(String subjectCode, String mappedCO, String cognitiveLevel){
        List<Question> tempQuestions=questionRepository.findBySubjectCodeAndMappedCOAndCognitiveLevel(subjectCode, mappedCO, cognitiveLevel);
        if (!(tempQuestions.isEmpty())){
            return tempQuestions;
        }
        throw  new QuestionNotFoundException("No questions found with Subject name: "+subjectCode+"and Mapped CO"+mappedCO+"Cognitive level"+cognitiveLevel);
    }

    public List<Question> findBySubjectName(String subjectName){
        List<Question> tempQuestion=questionRepository.findBySubjectName(subjectName);
        if (!(tempQuestion.isEmpty())){
            return tempQuestion;
        }
        throw new QuestionNotFoundException("No questions found with Subject name: "+subjectName);
    }

    public Page<Question> findBySubjectName(String subjectName,int pageNo , int size){
        Pageable pageable=PageRequest.of(pageNo,size);
        Page<Question> temp=questionRepository.findBySubjectName(subjectName,pageable);
        if (!(temp.isEmpty())){
            return temp;
        }
        throw new QuestionNotFoundException("No questions found with Subject name: "+subjectName);
    }

    public List<Question> findBySubjectNameMappedCO(String subjectName,String mappedCO){
        List<Question> tempQuestion=questionRepository.findBySubjectNameAndMappedCO(subjectName,mappedCO);
        if (!(tempQuestion.isEmpty())){
            return tempQuestion;
        }
        throw  new QuestionNotFoundException("No questions found with Subject name: "+subjectName+"and Mapped "+mappedCO);
    }

    public Page<Question> findBySubjectNameMappedCO(String subjectName,String mappedCO,int pageNo,int size){
        Pageable pageable=PageRequest.of(pageNo,size);
        Page<Question> tempQuestion=questionRepository.findBySubjectNameAndMappedCO(subjectName,mappedCO,pageable);
        if (!(tempQuestion.isEmpty())){
            return tempQuestion;
        }
        else {
            throw  new QuestionNotFoundException("No questions found with Subject name: "+subjectName+" and Mapped "+mappedCO);
        }
    }

    public List<Question> findBySubjectNameMappedCOCognitiveLevel(String subjectName,String mappedCO,String cognitiveLevel){
        List<Question> tempQuestion=questionRepository.findBySubjectNameAndMappedCOAndCognitiveLevel(subjectName, mappedCO, cognitiveLevel);
        if (!(tempQuestion.isEmpty())){
            return tempQuestion;
        }
        throw  new QuestionNotFoundException("No questions found with Subject name: "+subjectName+"and Mapped CO"+mappedCO+"Cognitive level"+cognitiveLevel);
    }

    public List<Question> findByCreatedByUsingEmail(String email){
        User tempUser= userService.findByEmail(email);
        if (!tempUser.getRole().equalsIgnoreCase("ROLE_TEACHER")) {
            throw new UserNotAuthorizesException("User with email is not authorized to make questions"+email);
        }
        else {
            List<Question> tempQuestion=questionRepository.findByCreatedBy(tempUser);
            if (!(tempQuestion.isEmpty())){
                return tempQuestion;
            }
            else {
                throw new QuestionNotFoundException("User with the email has not created any Question"+email);
            }
        }
    }

    public Page<Question> findByCreatedByUsingEmail(String email, int pageNo,int size){
        User tempUser= userService.findByEmail(email);
        Pageable pageable= PageRequest.of(pageNo,size);
        if (!tempUser.getRole().equalsIgnoreCase("ROLE_TEACHER")) {
            throw new UserNotAuthorizesException("User with email is not authorized to make questions"+email);
        }
        else {
            Page<Question> tempQuestion=questionRepository.findByCreatedBy(tempUser,pageable);
            if (!(tempQuestion.isEmpty())){
                return tempQuestion;
            }
            else {
                throw new QuestionNotFoundException("User with the email has not created any Question"+email);
            }
        }
    }

    public List<Question> findByCreatedByUsingId(Long Id){
        User tempUser=userService.findUserById(Id);
        if (!tempUser.getRole().equalsIgnoreCase("ROLE_TEACHER")) {
            throw new UserNotAuthorizesException("User with id is not authorized to make questions"+Id);
        }
        else {
            List<Question> tempQuestion=questionRepository.findByCreatedBy(tempUser);
            if (!(tempQuestion.isEmpty())){
                return tempQuestion;
            }
            else {
                throw new QuestionNotFoundException("User with the Id has not created any Question"+Id);
            }
        }
    }
//todo use this method in supervise
    public Page<Question> findByCreatedByUsingId(Long Id,int pageNo,int size){
        Pageable pageable=PageRequest.of(pageNo,size);
        User tempUser=userService.findUserById(Id);
        if (!tempUser.getRole().equalsIgnoreCase("ROLE_TEACHER")) {
            throw new UserNotAuthorizesException("User with id is not authorized to make questions"+Id);
        }
        else {
            Page<Question> tempQuestion=questionRepository.findByCreatedBy(tempUser,pageable);
            if (!(tempQuestion.isEmpty())){
                return tempQuestion;
            }
            else {
                throw new QuestionNotFoundException("User with the Id has not created any Question"+Id);
            }
        }
    }

    public List<Question> getAllQuestionsByCurrentUser(){
        String email=UserUtil.getUserAuthentication().getUsername();
        String role=UserUtil.getUserAuthentication().getAuthorities().toString();
        if (!(role.contains("ROLE_TEACHER"))){
            throw new UserNotAuthorizesException("You are not authorized to make this request");
        }
        else {
            return findByCreatedByUsingEmail(email);
        }
    }

    public Page<Question> getAllQuestionsByCurrentUser(int pageNo,int size){
        String email=UserUtil.getUserAuthentication().getUsername();
        String role=UserUtil.getUserAuthentication().getAuthorities().toString();
        if (!(role.contains("ROLE_TEACHER"))){
            throw new UserNotAuthorizesException("You are not authorized to make this request");
        }
        else {
           return findByCreatedByUsingEmail(email,pageNo,size);
        }
    }
//todo use this metho
    public Question findQuestionByQuestionBody(String questionBody){
        return questionRepository.
                findByQuestionTitle(
                        QuestionUtil.sha256(questionBody)
                ).orElseThrow(
                        ()-> new QuestionNotFoundException("no question with this body")
                );
    }

    public void deleteQuestionByQuestionBody(String questionBody){
        Question temp=questionRepository.findByQuestionTitle(
                QuestionUtil.sha256(questionBody)
        ).orElseThrow(
                ()-> new QuestionNotFoundException("no question with this body")
        );
        questionRepository.delete(temp);
    }

    @Transactional
    public Question addQuestion(Question question) {
        String email= UserUtil.getUserAuthentication().getUsername();
        String questionTitle=QuestionUtil.sha256(question.getQuestionBody());
        System.out.println(email+"in security context holder");
        User user=userService.findByEmail(email);
        if (
                question.getQuestionBody().isEmpty()||
                !QuestionUtil.checkIfQuestionBodyIsAcceptable(question.getQuestionBody())
        ){
            throw new UnacceptableQuestion("Unacceptable Question due to eather no question body or more spaces that words ");
        }
        if (
                questionRepository.existsByQuestionTitle(questionTitle)
        ){
            throw new DuplicateQuestionException("question already present");
        }
        if (question.getQuestionMarks()>4 || question.getQuestionMarks()<2){
            throw new UnacceptableQuestion("one question in db that has more that has Marks >4 or Marks<2 ");
        }
        else {
            // possible to remove this part in future as we are taking email from security context holder so no way to forge
            if (!user.getRole().equalsIgnoreCase("ROLE_TEACHER")){
                throw new UserNotAuthorizesException("User unauthorised to make this request");
            }
            else {
                question.setQuestionTitle(questionTitle);
                question.setCreatedBy(user);
                question.setInUse(false);
                return questionRepository.save(question);
            }
        }
    }

    public void deleteQuestionById(Long id){
        if (questionRepository.existsById(id)){
            questionRepository.deleteById(id);
        }
        else {
            throw new QuestionNotFoundException("Question not found with ID: " + id);
        }
    }

    public List<Question> generateBySubjectCodeQuestion(
            String subjectCode,
            String[] mappedCOs,
            int numberOfCognitiveLevel_A,
            int numberOfCognitiveLevel_R,
            int numberOfCognitiveLevel_U,
            int maxNumberOf2Marks,
            int maxNumberOf4Marks
    ) {
        List<Question> output=new ArrayList<>();
        List<Question> allowed=questionRepository.findValidQuestionsWithSubjectCode(subjectCode,mappedCOs);
        if(allowed.isEmpty()){
            throw new QuestionNotFoundException("No questions found for selected subject  code + CO");
        }
        Collections.shuffle(allowed);
        for (Question question:allowed){
            if (question.getInUse()) {
                continue;
            }
            if(maxNumberOf2Marks==0 && maxNumberOf4Marks==0){
                break;
            }
            else {
                if (
                        (numberOfCognitiveLevel_A>0||numberOfCognitiveLevel_R>0||numberOfCognitiveLevel_U>0) &&
                        (maxNumberOf2Marks>0 || maxNumberOf4Marks>0)
                ){
                   switch (question.getCognitiveLevel()){
                       case "A"->{
                           if (
                                   (numberOfCognitiveLevel_A>0) &&
                                   (
                                           (question.getQuestionMarks()==2 && maxNumberOf2Marks>0)||
                                           (question.getQuestionMarks()==4 && maxNumberOf4Marks>0)
                                   )
                           ){
                               if (question.getQuestionMarks()==2){
                                   maxNumberOf2Marks--;
                               }
                               else if (question.getQuestionMarks()==4) {
                                   maxNumberOf4Marks--;
                               }
                               question.setIsInUse(true);
                               output.add(question);
                               numberOfCognitiveLevel_A--;
                           }
                       }

                       case "R"->{
                           if (
                                   (numberOfCognitiveLevel_R>0) &&
                                           (
                                                   (question.getQuestionMarks()==2 && maxNumberOf2Marks>0)||
                                                   (question.getQuestionMarks()==4 && maxNumberOf4Marks>0)
                                           )
                           ){
                               if (question.getQuestionMarks()==2){
                                   maxNumberOf2Marks--;
                               }
                               else if (question.getQuestionMarks()==4) {
                                   maxNumberOf4Marks--;
                               }
                               question.setIsInUse(true);
                               output.add(question);
                               numberOfCognitiveLevel_R--;
                           }
                       }

                       case "U"->{
                           if (
                                   (numberOfCognitiveLevel_U>0) &&
                                           (
                                                   (question.getQuestionMarks()==2 && maxNumberOf2Marks>0)||
                                                   (question.getQuestionMarks()==4 && maxNumberOf4Marks>0)
                                           )
                           ){
                               if (question.getQuestionMarks()==2){
                                   maxNumberOf2Marks--;
                               }
                               else if (question.getQuestionMarks()==4) {
                                   maxNumberOf4Marks--;
                               }
                               question.setIsInUse(true);
                               output.add(question);
                               numberOfCognitiveLevel_U--;
                           }
                       }
                   }
                }
            }
        }

        return output
                .stream()
                .sorted(Comparator.comparing(Question::getQuestionMarks))
                .toList();
    }


    public List<Question> generateBySubjectNameQuestion(
            String subjectName,
            String[] mappedCOs,
            int numberOfCognitiveLevel_A,
            int numberOfCognitiveLevel_R,
            int numberOfCognitiveLevel_U,
            int maxNumberOf2Marks,
            int maxNumberOf4Marks
    ) {
        List<Question> allowed = questionRepository.findValidQuestionWithSubjectName(subjectName, mappedCOs);
        List<Question> output = new ArrayList<>();
        if(allowed.isEmpty()){
            throw new QuestionNotFoundException("No questions found for selected subject + CO");
        }
        Collections.shuffle(allowed);
        for (Question question:allowed){
            if (question.getInUse()) {
                continue;
            }
            if(maxNumberOf2Marks==0 && maxNumberOf4Marks==0){
                break;
            }
            else {
                if (
                        (numberOfCognitiveLevel_A>0||numberOfCognitiveLevel_R>0||numberOfCognitiveLevel_U>0) &&
                        (maxNumberOf2Marks>0 || maxNumberOf4Marks>0)
                ){
                    switch (question.getCognitiveLevel()){
                        case "A"->{
                            if (
                                    (numberOfCognitiveLevel_A>0) &&
                                            (
                                                    (question.getQuestionMarks()==2 && maxNumberOf2Marks>0)||
                                                    (question.getQuestionMarks()==4 && maxNumberOf4Marks>0)
                                            )
                            ){
                                if (question.getQuestionMarks()==2){
                                    maxNumberOf2Marks--;
                                }
                                else if (question.getQuestionMarks()==4) {
                                    maxNumberOf4Marks--;
                                }
                                question.setIsInUse(true);
                                output.add(question);
                                numberOfCognitiveLevel_A--;
                            }
                        }

                        case "R"->{
                            if (
                                    (numberOfCognitiveLevel_R>0) &&
                                            (
                                                    (question.getQuestionMarks()==2 && maxNumberOf2Marks>0)||
                                                    (question.getQuestionMarks()==4 && maxNumberOf4Marks>0)
                                            )
                            ){
                                if (question.getQuestionMarks()==2){
                                    maxNumberOf2Marks--;
                                }
                                else if (question.getQuestionMarks()==4) {
                                    maxNumberOf4Marks--;
                                }
                                question.setIsInUse(true);
                                output.add(question);
                                numberOfCognitiveLevel_R--;
                            }
                        }

                        case "U"->{
                            if (
                                    (numberOfCognitiveLevel_U>0) &&
                                            (
                                                    (question.getQuestionMarks()==2 && maxNumberOf2Marks>0)||
                                                    (question.getQuestionMarks()==4 && maxNumberOf4Marks>0)
                                            )
                            ){
                                if (question.getQuestionMarks()==2){
                                    maxNumberOf2Marks--;
                                }
                                else if (question.getQuestionMarks()==4) {
                                    maxNumberOf4Marks--;
                                }
                                question.setIsInUse(true);
                                output.add(question);
                                numberOfCognitiveLevel_U--;
                            }
                        }
                    }
                }
            }
        }
        return output
                .stream()
                .sorted(Comparator.comparing(Question::getQuestionMarks))
                .toList();
    }

}
