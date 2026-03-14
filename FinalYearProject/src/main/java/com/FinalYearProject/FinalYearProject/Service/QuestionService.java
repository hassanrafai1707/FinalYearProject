package com.FinalYearProject.FinalYearProject.Service;

import com.FinalYearProject.FinalYearProject.Domain.User;
import com.FinalYearProject.FinalYearProject.Exceptions.DepartmentMissMatchException;
import com.FinalYearProject.FinalYearProject.Exceptions.QuestionException.DuplicateQuestionException;
import com.FinalYearProject.FinalYearProject.Exceptions.QuestionException.QuestionNotFoundException;
import com.FinalYearProject.FinalYearProject.Exceptions.QuestionException.UnacceptableQuestion;
import com.FinalYearProject.FinalYearProject.Domain.Question;
import com.FinalYearProject.FinalYearProject.Exceptions.UserEeceptions.UserNotAuthorizesException;
import com.FinalYearProject.FinalYearProject.Exceptions.UserEeceptions.WrongPasswordException;
import com.FinalYearProject.FinalYearProject.Repository.QuestionRepository;
import com.FinalYearProject.FinalYearProject.Util.QuestionUtil;
import com.FinalYearProject.FinalYearProject.Util.UserUtil;
import lombok.SneakyThrows;
import org.apache.coyote.BadRequestException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.*;

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
@Service
public class QuestionService {

    private final QuestionRepository questionRepository;
    private final UserService userService;

    public QuestionService( QuestionRepository questionRepository, UserService userService){
        this.questionRepository=questionRepository;
        this.userService=userService;
    }

    public List<Question> getAllQuestion(){
        List<Question> tempQuestion=questionRepository.findByDepartment(UserUtil.getUserAuthentication().getUser().getDepartment());
        if (!(tempQuestion.isEmpty())){
            return tempQuestion;
        }
        else {
            throw new QuestionNotFoundException("no Question in DataBase");
        }
    }

    public Page<Question> getAllQuestionsPaged(int pageNo,int size){
        Page<Question> temp=questionRepository.findByDepartment(UserUtil.getUserAuthentication().getUser().getDepartment(),PageRequest.of(pageNo,size));
        if (!(temp.isEmpty())){
            return temp;
        }
        else {
            throw new QuestionNotFoundException("no more Question in DataBase");
        }
    }

    public Question getQuestionById(Long id) {
        Question question=questionRepository.findById(id)
                .orElseThrow(() -> new QuestionNotFoundException("Question not found with ID: " + id));
        if (QuestionUtil.DepartmentCheck(question)){
            return question;
        }
        throw new DepartmentMissMatchException("you are not allowed to access this as it is not from your department");
    }

    //todo remove this
    @SneakyThrows
    public List<Question> getQuestionByIds(List<Long> Ids){
        if (Ids.size()>50) throw new BadRequestException(" you are question for too many question at the same time ");
        List<Question> temp =questionRepository.findAllById(Ids);
        if (temp.isEmpty()){
            throw new QuestionNotFoundException("question with the given ids not found");
        }
        if (QuestionUtil.DepartmentCheck(temp)){
            return temp;
        }
        throw new DepartmentMissMatchException("you are not allowed to access this as it is not from your department");
    }

    public List<Question> findBySubjectCode(String subjectCode){
        List<Question> tempQuestion=questionRepository.findBySubjectCode(subjectCode,UserUtil.getUserAuthentication().getUser().getDepartment());
        if (!(tempQuestion.isEmpty())){
            return tempQuestion;
        }
        throw new QuestionNotFoundException("No questions found with Subject code: "+subjectCode);
    }

    public Page<Question> findBySubjectCode(String subjectCode,int pageNo,int size){
        Page<Question> temp = questionRepository.findBySubjectCode(
                subjectCode,
                UserUtil.getUserAuthentication().getUser().getDepartment(),
                PageRequest.of(pageNo,size)
        );
        if (!(temp.isEmpty())){
            return temp;
        }
        throw new QuestionNotFoundException("No questions found with Subject code: "+subjectCode);
    }

    public List<Question> findBySubjectCodeMappedCO(String subjectCode , String mappedCO){
        List<Question> tempQuestion=questionRepository.findBySubjectCodeAndMappedCO(
                subjectCode,
                mappedCO,
                UserUtil.getUserAuthentication().getUser().getDepartment()
        );
        if (!(tempQuestion.isEmpty())){
            return tempQuestion;
        }
        throw  new QuestionNotFoundException("No questions found with Subject name: "+subjectCode+"and Mapped CO"+mappedCO);
    }

    public Page<Question> findBySubjectCodeMappedCO(String subjectCode,String mappedCO, int pageNo, int size){
        Page<Question> tempQuestions=questionRepository.findBySubjectCodeAndMappedCO(
                subjectCode,
                mappedCO,
                UserUtil.getUserAuthentication().getUser().getDepartment(),
                PageRequest.of(pageNo,size)
        );
        if (!(tempQuestions.isEmpty())){
            return tempQuestions;
        }
        else {
            throw  new QuestionNotFoundException("No questions found with Subject name: "+subjectCode+"and Mapped CO"+mappedCO);
        }
    }

    public List<Question> findBySubjectCodeMappedCOCognitiveLevel(
            String subjectCode,
            String mappedCO,
            String cognitiveLevel
    ){
        List<Question> tempQuestions=questionRepository.findBySubjectCodeAndMappedCOAndCognitiveLevel(
                subjectCode,
                mappedCO,
                UserUtil.getUserAuthentication().getUser().getDepartment(),
                cognitiveLevel);
        if (!(tempQuestions.isEmpty())){
            return tempQuestions;
        }
        throw  new QuestionNotFoundException("No questions found with Subject name: "+subjectCode+"and Mapped CO"+mappedCO+"Cognitive level"+cognitiveLevel);
    }

    public Page<Question> findBySubjectCodeMappedCOCognitiveLevel(
            String subjectCode,
            String mappedCO,
            String cognitiveLevel,
            int pageNo,
            int size
            ){
        Page<Question> questions=questionRepository.findBySubjectCodeAndMappedCOAndCognitiveLevel(
                subjectCode,
                mappedCO,

                UserUtil.getUserAuthentication().getUser().getDepartment(),
                cognitiveLevel,
                PageRequest.of(pageNo,size)
        );
        if (questions.isEmpty()){
            throw new QuestionNotFoundException("No questions found with Subject name: "+subjectCode+"and Mapped CO"+mappedCO+"Cognitive level"+cognitiveLevel);
        }
        return questions;
    }

    public List<Question> findBySubjectName(String subjectName){
        List<Question> tempQuestion=questionRepository.findBySubjectName(subjectName,UserUtil.getUserAuthentication().getUser().getDepartment());
        if (!(tempQuestion.isEmpty())){
            return tempQuestion;
        }
        throw new QuestionNotFoundException("No questions found with Subject name: "+subjectName);
    }

    public Page<Question> findBySubjectName(String subjectName,int pageNo , int size){
        Page<Question> temp=questionRepository.findBySubjectName(
                subjectName,
                UserUtil.getUserAuthentication().getUser().getDepartment(),
                PageRequest.of(pageNo,size)
        );
        if (!(temp.isEmpty())){
            return temp;
        }
        throw new QuestionNotFoundException("No questions found with Subject name: "+subjectName);
    }

    public List<Question> findBySubjectNameMappedCO(String subjectName,String mappedCO){
        List<Question> tempQuestion=questionRepository.findBySubjectNameAndMappedCO(
                subjectName,
                mappedCO,
                UserUtil.getUserAuthentication().getUser().getDepartment()
        );
        if (!(tempQuestion.isEmpty())){
            return tempQuestion;
        }
        throw  new QuestionNotFoundException("No questions found with Subject name: "+subjectName+"and Mapped "+mappedCO);
    }

    public Page<Question> findBySubjectNameMappedCO(String subjectName,String mappedCO,int pageNo,int size){
        Page<Question> tempQuestion=questionRepository.findBySubjectNameAndMappedCO(
                subjectName,
                mappedCO,
                UserUtil.getUserAuthentication().getUser().getDepartment(),
                PageRequest.of(pageNo,size)
        );
        if (!(tempQuestion.isEmpty())){
            return tempQuestion;
        }
        else {
            throw  new QuestionNotFoundException("No questions found with Subject name: "+subjectName+" and Mapped "+mappedCO);
        }
    }

    public List<Question> findBySubjectNameMappedCOCognitiveLevel(
            String subjectName,
            String mappedCO,
            String cognitiveLevel
    ){
        List<Question> tempQuestion=questionRepository.findBySubjectNameAndMappedCOAndCognitiveLevel(
                subjectName,
                mappedCO,
                UserUtil.getUserAuthentication().getUser().getDepartment(),
                cognitiveLevel
        );
        if (!(tempQuestion.isEmpty())){
            return tempQuestion;
        }
        throw  new QuestionNotFoundException("No questions found with Subject name: "+subjectName+"and Mapped CO"+mappedCO+"Cognitive level"+cognitiveLevel);
    }

    public Page<Question> findBySubjectNameMappedCOCognitiveLevel(
            String subjectName,
            String mappedCO,
            String cognitiveLevel,
            int pageNo,
            int size
    ){
        Page<Question> questions=questionRepository.findBySubjectNameAndMappedCOAndCognitiveLevel(
                subjectName,
                mappedCO,
                UserUtil.getUserAuthentication().getUser().getDepartment(),
                cognitiveLevel,
                PageRequest.of(pageNo,size)
        );
        if (questions.isEmpty()){
            throw new QuestionNotFoundException("No questions found with Subject name: "+subjectName+"and Mapped CO"+mappedCO+"Cognitive level"+cognitiveLevel);
        }
        return questions;
    }

    @PreAuthorize("ROLE_SUPERVISOR")
    public List<Question> findByCreatedByUsingEmail(String email){
        User user=userService.findByEmail(email);
        if (!user.getDepartment().equals(UserUtil.getUserAuthentication().getUser().getDepartment())){
            throw new DepartmentMissMatchException("You are not allowed to access as you are not from the same Department as user with email:"+email);
        }
        List<Question> tempQuestion=questionRepository.findByCreatedBy(user);
        if (!(tempQuestion.isEmpty())){
            return tempQuestion;
        }
        else {
            throw new QuestionNotFoundException("User with the email has not created any Question"+email);
        }
    }

    @PreAuthorize("ROLE_SUPERVISOR")
    public Page<Question> findByCreatedByUsingEmail(String email, int pageNo,int size){
        User user=userService.findByEmail(email);
        if (!user.getDepartment().equals(UserUtil.getUserAuthentication().getUser().getDepartment())){
            throw new DepartmentMissMatchException("You are not allowed to access as you are not from the same Department as user with email:"+email);
        }
        Page<Question> tempQuestion=questionRepository.findByCreatedBy(
              user,
              PageRequest.of(pageNo,size)
        );
        if (!(tempQuestion.isEmpty())){
            return tempQuestion;
        }
        else {
            throw new QuestionNotFoundException("User with the email has not created any Question"+email);
        }
    }

    @PreAuthorize("ROLE_SUPERVISOR")
    public List<Question> findByCreatedByUsingId(Long Id){
        User user=userService.findUserById(Id);
        if (!user.getDepartment().equals(UserUtil.getUserAuthentication().getUser().getDepartment())){
            throw new DepartmentMissMatchException("You are not allowed to access as you are not from the same Department as user with Id:"+Id);
        }
        List<Question> tempQuestion=questionRepository.findByCreatedBy(user);
        if (!(tempQuestion.isEmpty())){
            return tempQuestion;
        }
        else {
            throw new QuestionNotFoundException("User with the Id has not created any Question"+Id);
        }
    }

    @PreAuthorize("ROLE_SUPERVISOR")
    public Page<Question> findByCreatedByUsingId(Long Id,int pageNo,int size){
        User user=userService.findUserById(Id);
        if (!user.getDepartment().equals(UserUtil.getUserAuthentication().getUser().getDepartment())){
            throw new DepartmentMissMatchException("You are not allowed to access as you are not from the same Department as user with Id:"+Id);
        }
        Page<Question> tempQuestion=questionRepository.findByCreatedBy(
                user,
                PageRequest.of(pageNo,size)
        );
        if (!(tempQuestion.isEmpty())){
            return tempQuestion;
        }
        else {
            throw new QuestionNotFoundException("User with the Id has not created any Question"+Id);
        }
    }

    @PreAuthorize("ROLE_TEACHER")
    public List<Question> getAllQuestionsByCurrentUser(){
        List<Question> questions=questionRepository.findByCreatedBy(
                userService.findByEmail(
                        UserUtil.getUserAuthentication().getUsername()
                )
        );
        if (questions.isEmpty()){
            throw new QuestionNotFoundException("you have not created any questions");
        }
        return questions;
    }

    @PreAuthorize("ROLE_TEACHER")
    public Page<Question> getAllQuestionsByCurrentUser(int pageNo,int size){
        Page<Question> questionPage=questionRepository.findByCreatedBy(
                userService.findByEmail(
                        UserUtil.getUserAuthentication().getUsername()
                ),
                PageRequest.of(pageNo, size)
        );
        if (questionPage.isEmpty()||questionPage.getContent().isEmpty()){
            throw new QuestionNotFoundException("you have not created any questions");
        }
        return questionPage;
    }

    @SneakyThrows
    @Transactional
    @PreAuthorize("ROLE_ADMIN")
    public void updateCreatedByUsingEmail(String replaceEmail,String originalEmail,String password){
        if (
                replaceEmail.isEmpty()||
                originalEmail.isEmpty()||
                password.isEmpty()||
                !(
                        userService.existsByEmail(replaceEmail)&&userService.existsByEmail(originalEmail)
                )
        ){
           throw new BadRequestException("this request is invalid because one of the given parameter is empty");
        }
        if (
                !userService.matchPasswords(
                    password,
                    UserUtil.getUserAuthentication().getPassword()
                )
        ){
            throw new WrongPasswordException("your password doesn't match");
        }
        User replaceUser =userService.findByEmail(replaceEmail);
        if (!replaceUser.getRole().contains("ROLE_TEACHER")){
            throw new UserNotAuthorizesException("the selected user is not Authorizes to make this change");
        }
        List<Question> questions=new ArrayList<>();
        for (Question question:questionRepository.findByCreatedBy(
                userService.findByEmail(originalEmail)
        )){
            question.setCreatedBy(replaceUser);
            questions.add(question);
        }
        questionRepository.saveAll(questions);
    }

    @SneakyThrows
    @Transactional
    @PreAuthorize("ROLE_ADMIN")
    public void updateCreatedByUsingId(Long replaceID,Long originalID, String password){
        if (
                !(
                        userService.existsById(replaceID) &&
                        userService.existsById(originalID)
                )
        ){
            throw new BadRequestException("the ids given is not valid");
        }
        User replaceUser =userService.findUserById(replaceID);
        if (!replaceUser.getRole().contains("ROLE_TEACHER")){
            throw new UserNotAuthorizesException("user with email:"+ replaceUser.getEmail()+"has role:"+ replaceUser.getRole()+" and not role ROLE_TEACHER");
        }
        if(userService.matchPasswords(password,UserUtil.getUserAuthentication().getPassword())){//the userUtil is admin user
            List<Question> result=new ArrayList<>();
            for (Question question:questionRepository.findByCreatedBy(
                    userService.findUserById(
                            originalID
                    )
            )){
                question.setCreatedBy(replaceUser);
                result.add(question);
            }
            questionRepository.saveAll(result);
        }
        else {
            throw new WrongPasswordException("your password is wrong");
        }
    }

    @Transactional
    @PreAuthorize("ROLE_TEACHER")
    public Question addQuestion(Question question) {
        String email= UserUtil.getUserAuthentication().getUsername();
        String questionTitle=QuestionUtil.sha256(question.getQuestionBody());
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
        if (!(question.getQuestionMarks()==2||question.getQuestionMarks()==4)){
            throw new UnacceptableQuestion("one question in db that has more that has Marks >4 or Marks<2 ");
        }
        question.setQuestionTitle(questionTitle);
        question.setCreatedBy(user);
        question.setInUse(false);
        return questionRepository.save(question);
    }

    @Transactional
    @PreAuthorize("ROLE_TEACHER")
    public void deleteQuestionById(Long id){
        if (!questionRepository.existsById(id)){
            throw new QuestionNotFoundException("Question not found with ID: " + id);
        }
        else {
            questionRepository.deleteById(id);
        }
    }

    @PreAuthorize("ROLE_TEACHER")
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
        List<Question> allowed=questionRepository.findValidQuestionsWithSubjectCode(
                subjectCode,
                mappedCOs,
                UserUtil.getUserAuthentication().getUser().getDepartment()
        );
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


    @PreAuthorize("ROLE_TEACHER")
    public List<Question> generateBySubjectNameQuestion(
            String subjectName,
            String[] mappedCOs,
            int numberOfCognitiveLevel_A,
            int numberOfCognitiveLevel_R,
            int numberOfCognitiveLevel_U,
            int maxNumberOf2Marks,
            int maxNumberOf4Marks
    ) {
        List<Question> allowed = questionRepository.findValidQuestionWithSubjectName(
                subjectName,
                mappedCOs,
                UserUtil.getUserAuthentication().getUser().getDepartment()
        );
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
