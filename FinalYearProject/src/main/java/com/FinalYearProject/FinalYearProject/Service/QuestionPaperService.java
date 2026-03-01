package com.FinalYearProject.FinalYearProject.Service;


import com.FinalYearProject.FinalYearProject.Domain.Question;
import com.FinalYearProject.FinalYearProject.Domain.QuestionPaper;
import com.FinalYearProject.FinalYearProject.Domain.User;
import com.FinalYearProject.FinalYearProject.Exceptions.QuestionPaperException.DuplicateQuestionPaperException;
import com.FinalYearProject.FinalYearProject.Exceptions.QuestionPaperException.QuestionPaperNotFoundException;
import com.FinalYearProject.FinalYearProject.Exceptions.UserEeceptions.UserNotAuthorizesException;
import com.FinalYearProject.FinalYearProject.Exceptions.UserEeceptions.WrongPasswordException;
import com.FinalYearProject.FinalYearProject.Repository.QuestionPaperRepository;
import com.FinalYearProject.FinalYearProject.Util.QuestionPaperUtil;
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
 * QuestionPaperService - Business Logic Service for Exam Paper Management
 * PURPOSE: Core service for exam paper operations including creation, retrieval, approval workflows, and integrity validation. Manages complete paper lifecycle.
 * PAPER LIFECYCLE MANAGEMENT: Handles paper creation by teachers, approval by supervisors, and retrieval by all roles. Maintains approval status and audit trail.
 * APPROVAL WORKFLOW: approveQuestionPaperById/ByTile and notApproveQuestionPaperById/ByTile methods implement supervisor approval/rejection with authorization checks.
 * INTEGRITY VALIDATION: Uses SHA256 fingerprinting (QuestionPaperUtil) to detect duplicate papers. Validates all referenced questions exist before paper creation.
 * AUTHORIZATION ENFORCEMENT: Role-based checks for all operations - teachers create papers, supervisors approve, all roles can view based on permissions.
 * PAGINATION SUPPORT: All list methods support pagination via Pageable. Returns Page objects for efficient large dataset handling.
 * TRANSACTION MANAGEMENT: @Transactional on write operations ensures data consistency. Critical for paper creation and approval updates.
 * USER CONTEXT: Uses UserUtil.getUserAuthentication() to identify current user for ownership and authorization checks. Maintains generatedBy and approvedBy audit trail.
 * ERROR HANDLING: Comprehensive exception handling - QuestionPaperNotFoundException for missing papers, UserNotAuthorizesException for permission violations, DuplicateQuestionPaperException for duplicates.
 * FINGERPRINT GENERATION: Creates content-based fingerprint from sorted question IDs. Enables duplicate detection even with different paper titles.
 * INTEGRATION: Works with QuestionService for question validation, UserService for user lookups, and QuestionPaperRepository for data persistence.
 */
@Service
public class QuestionPaperService {
    private final QuestionPaperRepository questionPaperRepository;
    private final UserService userService;
    private final QuestionService questionService;

    public QuestionPaperService(
            QuestionPaperRepository questionPaperRepository,
            UserService userService,
            QuestionService questionService
    ){
        this.questionService=questionService;
        this.questionPaperRepository=questionPaperRepository;
        this.userService=userService;
    }

    @PreAuthorize("ROLE_SUPERVISOR")
    public List<QuestionPaper> getAllQuestionPapers(){
        List<QuestionPaper> questionPapers=questionPaperRepository.findAll();
        if (questionPapers.isEmpty()){
            throw new QuestionPaperNotFoundException("no question paper in db");
        }
        else {
            return questionPapers;
        }
    }

    @PreAuthorize("ROLE_SUPERVISOR")
    public Page<QuestionPaper> getAllQuestionPapers(int pageNo , int size){
        Page<QuestionPaper> questionPaperPage=questionPaperRepository.findAll(PageRequest.of(pageNo,size));
        if (!(questionPaperPage.isEmpty())){
            return questionPaperPage;
        }
        else {
            throw new QuestionPaperNotFoundException("no question paper in db ");
        }
    }

    @PreAuthorize("ROLE_SUPERVISOR")
    public QuestionPaper findById(Long Id){
        return questionPaperRepository.findById(Id)
                .orElseThrow(()-> new QuestionPaperNotFoundException("no question paper with id"+Id));
    }

    @PreAuthorize("ROLE_SUPERVISOR")
    public QuestionPaper findByExamTitle(String examTitle) {
        return questionPaperRepository.findByExamTitle(examTitle)
                .orElseThrow(()-> new QuestionPaperNotFoundException("no question paper with exam title"+examTitle));
    }

    @PreAuthorize("ROLE_SUPERVISOR")
    public List<QuestionPaper> findByGeneratedByUsingEmail(String email){
        List<QuestionPaper> questionPapersGeneratedByUser=questionPaperRepository.findByGeneratedBy(
                userService.findByEmail(email)
        );
        if (questionPapersGeneratedByUser.isEmpty()){
            throw new QuestionPaperNotFoundException("no question paper has been generated by user with email "+email);
        }
        else {
            return questionPapersGeneratedByUser;
        }
    }

    @PreAuthorize("ROLE_SUPERVISOR")
    public Page<QuestionPaper> findByGeneratedByUsingEmail(String email, int pageNo, int size){
        Page<QuestionPaper> questionPapersGeneratedByUser=questionPaperRepository.findByGeneratedBy(
                userService.findByEmail(email),PageRequest.of(pageNo,size)
        );
        if (!(questionPapersGeneratedByUser.isEmpty())){
            return questionPapersGeneratedByUser;
        }
        else {
            throw new QuestionPaperNotFoundException("no question paper has been generated by user with email "+email);
        }
    }

    @PreAuthorize("ROLE_SUPERVISOR")
    public List<QuestionPaper> findByGeneratedByUsingId(Long Id){
        List<QuestionPaper> questionPapers=questionPaperRepository.findByGeneratedBy(userService.findUserById(Id));
        if (questionPapers.isEmpty()){
            throw new QuestionPaperNotFoundException("no question paper has been generated by user with Id "+Id);
        }
        else {
            return questionPapers;
        }
    }

    @PreAuthorize("ROLE_SUPERVISOR")
    public Page<QuestionPaper> findByGeneratedByUsingId(Long Id,int pageNo,int size){
        Page<QuestionPaper> questionPapers=questionPaperRepository.findByGeneratedBy(
                userService.findUserById(Id),PageRequest.of(pageNo,size)
        );
        if (!(questionPapers.isEmpty())){
            return questionPapers;
        }
        else {
            throw new QuestionPaperNotFoundException("no question paper has been generated by user with Id "+Id);
        }
    }

    @PreAuthorize("ROLE_SUPERVISOR")
    public List<QuestionPaper> findByApprovedByUsingEmail(String email){
        List<QuestionPaper> questionPapers=questionPaperRepository.findByApprovedBy(userService.findByEmail(email));
        if (questionPapers.isEmpty()){
            throw new QuestionPaperNotFoundException("no question paper has been generated by user with email "+email);
        }
        else {
            return questionPapers;
        }
    }

    @PreAuthorize("ROLE_SUPERVISOR")
    public  Page<QuestionPaper> findByApprovedByUsingEmail(String email, int pageNo , int size){
        Page<QuestionPaper> questionPapers=questionPaperRepository.findByApprovedBy(
                userService.findByEmail(email),PageRequest.of(pageNo,size)
        );
        if (!(questionPapers.isEmpty())){
            return questionPapers;
        }
        else {
            throw new QuestionPaperNotFoundException("no question paper has been generated by user with email "+email);

        }
    }

    @PreAuthorize("ROLE_SUPERVISOR")
    public List<QuestionPaper> findByApprovedByUsingId(Long Id){
        User user=userService.findUserById(Id);
        List<QuestionPaper> questionPapers=questionPaperRepository.findByApprovedBy(user);
        if (questionPapers.isEmpty()){
            throw new QuestionPaperNotFoundException("no question paper has been generated by user with Id "+Id);
        }
        else {
            return questionPapers;
        }
    }

    @PreAuthorize("ROLE_SUPERVISOR")
    public Page<QuestionPaper> findByApprovedByUsingId(Long Id,int pageNo,int size){
        User user=userService.findUserById(Id);
        Page<QuestionPaper> questionPapers=questionPaperRepository.findByApprovedBy(user,PageRequest.of(pageNo,size));
        if (!(questionPapers.isEmpty())){
            return questionPapers;
        }
        else {
            throw new QuestionPaperNotFoundException("no question paper has been generated by user with Id "+Id);
        }
    }

    @PreAuthorize("ROLE_SUPERVISOR")
    public List<QuestionPaper> findApproved(){
        List<QuestionPaper> questionPapers=questionPaperRepository.findByApproved(Boolean.TRUE);
        if (questionPapers.isEmpty()){
            throw new QuestionPaperNotFoundException("no question paper has been approved yet ");
        }
        else {
            return questionPapers;
        }
    }

    @PreAuthorize("ROLE_SUPERVISOR")
    public Page<QuestionPaper> findApproved(int pageNo,int size){
        Page<QuestionPaper> questionPapers=questionPaperRepository.findByApproved(
                Boolean.TRUE,PageRequest.of(pageNo,size)
        );
        if (!(questionPapers.isEmpty())){
            return questionPapers;
        }
        else {
            throw new QuestionPaperNotFoundException("no question paper has been approved yet ");
        }
    }

    @PreAuthorize("ROLE_SUPERVISOR")
    public List<QuestionPaper> findNotApproved(){
        List<QuestionPaper> questionPapers=questionPaperRepository.findByApproved(Boolean.FALSE);
        if (questionPapers.isEmpty()){
            throw new QuestionPaperNotFoundException("no question paper has been left to approve");
        }
        else {
            return questionPapers;
        }
    }

    @PreAuthorize("ROLE_SUPERVISOR")
    public Page<QuestionPaper> findNotApproved(int pageNo,int size){
        Page<QuestionPaper> questionPapers=questionPaperRepository.findByApproved(
                Boolean.FALSE,PageRequest.of(pageNo,size)
        );
        if (!(questionPapers.isEmpty())){
            return questionPapers;
        }
        else {
            throw new QuestionPaperNotFoundException("no question paper has been approved yet ");
        }
    }

    @Transactional
    @PreAuthorize("ROLE_SUPERVISOR")
    public QuestionPaper approveQuestionPaperById(Long id){
        QuestionPaper questionPaper=questionPaperRepository
                .findById(id)
                .orElseThrow(
                        ()-> new QuestionPaperNotFoundException("no Question Paper with id :" +id)
                );
        if (questionPaper.getApproved().equals(Boolean.TRUE)){
            return questionPaper;
        }
        else {
            questionPaper.setApproved(Boolean.TRUE);
            questionPaper.setApprovedBy(UserUtil.getUserAuthentication().getUser());
            questionPaperRepository.save(questionPaper);
            return questionPaper;
        }
    }

    @Transactional
    @PreAuthorize("ROLE_SUPERVISOR")
    public QuestionPaper notApproveQuestionPaperById(Long id){
        QuestionPaper questionPaper=questionPaperRepository
                .findById(id)
                .orElseThrow(
                        ()-> new QuestionPaperNotFoundException("no Question Paper with id :" +id)
                );
        if (questionPaper.getApproved().equals(Boolean.FALSE)){
                return questionPaper;
        }
        else {
            questionPaper.setApproved(Boolean.FALSE);
            questionPaper.setApprovedBy(UserUtil.getUserAuthentication().getUser());
            questionPaperRepository.save(questionPaper);
            return questionPaper;
        }
    }

    @Transactional
    @PreAuthorize("ROLE_SUPERVISOR")
    public QuestionPaper approvedQuestionPaperByTile(String examTitle){
        QuestionPaper questionPaper = questionPaperRepository
                .findByExamTitle(examTitle)
                .orElseThrow(
                        ()-> new QuestionPaperNotFoundException("question paper with title :"+examTitle+" does not exist ")
                );
        if (questionPaper.getApproved().equals(Boolean.TRUE)){
            return questionPaper;
        }
        else {
            questionPaper.setApproved(Boolean.TRUE);
            questionPaper.setApprovedBy(UserUtil.getUserAuthentication().getUser());
            questionPaperRepository.save(questionPaper);
            return questionPaper;
        }
    }

    @Transactional
    @PreAuthorize("ROLE_SUPERVISOR")
    public QuestionPaper notApprovedQuestionPaperByTile(String examTitle){
        QuestionPaper questionPaper = questionPaperRepository
                .findByExamTitle(examTitle)
                .orElseThrow(
                        ()-> new QuestionPaperNotFoundException("question paper with title :"+examTitle+" does not exist ")
                );
        if (questionPaper.getApproved().equals(Boolean.FALSE)){
            return questionPaper;
        }
        else {
            questionPaper.setApproved(Boolean.FALSE);
            questionPaper.setApprovedBy(UserUtil.getUserAuthentication().getUser());
            questionPaperRepository.save(questionPaper);
            return questionPaper;
        }
    }

    @Transactional
    @PreAuthorize("ROLE_ADMIN")
    public List<QuestionPaper> updateGeneratedByUsingEmail(String replaceEmail,String originalEmail,String password){
        User replaceUser =userService.findByEmail(replaceEmail);
        if (replaceUser.getRole().contains("ROLE_TEACHER")){
            throw new UserNotAuthorizesException("user with email:"+ replaceUser.getEmail()+"has role:"+ replaceUser.getRole()+" and not role ROLE_TEACHER");
        }
        if(userService.matchPasswords(password,UserUtil.getUserAuthentication().getPassword())){//the userutil is admin user
            List<QuestionPaper> result=new ArrayList<>();
            for (QuestionPaper questionPaper:questionPaperRepository.findByGeneratedBy(
                    userService.findByEmail(
                            originalEmail
                    )
            )){
                questionPaper.setGeneratedBy(replaceUser);
                result.add(questionPaper);
            }
            return questionPaperRepository.saveAll(result);
        }
        else {
            throw new WrongPasswordException("your password is wrong");
        }
    }

    @Transactional
    @PreAuthorize("ROLE_ADMIN")
    public List<QuestionPaper> updateGeneratedByUsingId(Long replaceID,Long originalID,String password){
        User replaceUser =userService.findUserById(replaceID);
        if (replaceUser.getRole().contains("ROLE_TEACHER")){
            throw new UserNotAuthorizesException("user with email:"+ replaceUser.getEmail()+"has role:"+ replaceUser.getRole()+" and not role ROLE_TEACHER");
        }
        if(userService.matchPasswords(password,UserUtil.getUserAuthentication().getPassword())){//the userUtil is admin user
            List<QuestionPaper> result=new ArrayList<>();
            for (QuestionPaper questionPaper:questionPaperRepository.findByGeneratedBy(
                    userService.findUserById(
                            originalID
                    )
            )){
                questionPaper.setGeneratedBy(replaceUser);
                result.add(questionPaper);
            }
            return questionPaperRepository.saveAll(result);
        }
        else {
            throw new WrongPasswordException("your password is wrong");
        }
    }

    @Transactional
    @PreAuthorize("ROLE_ADMIN")
    public List<QuestionPaper> updateApprovedByUsingEmail(String replaceEmail,String originalEmail,String password){
        User replaceUser =userService.findByEmail(replaceEmail);
        if (replaceUser.getRole().contains("ROLE_TEACHER")){
            throw new UserNotAuthorizesException("user with email:"+ replaceUser.getEmail()+"has role:"+ replaceUser.getRole()+" and not role ROLE_TEACHER");
        }
        if(userService.matchPasswords(password,UserUtil.getUserAuthentication().getPassword())){//the userutil is admin user
            List<QuestionPaper> result=new ArrayList<>();
            for (QuestionPaper questionPaper:questionPaperRepository.findByGeneratedBy(
                    userService.findByEmail(
                            originalEmail
                    )
            )){
                questionPaper.setApprovedBy(replaceUser);
                result.add(questionPaper);
            }
            return questionPaperRepository.saveAll(result);
        }
        else {
            throw new WrongPasswordException("your password is wrong");
        }
    }

    @Transactional
    @PreAuthorize("ROLE_ADMIN")
    public List<QuestionPaper> updateApprovedByUsingId(Long replaceID,Long originalID,String password){
        User replaceUser =userService.findUserById(replaceID);
        if (replaceUser.getRole().contains("ROLE_TEACHER")){
            throw new UserNotAuthorizesException("user with email:"+ replaceUser.getEmail()+"has role:"+ replaceUser.getRole()+" and not role ROLE_TEACHER");
        }
        if(userService.matchPasswords(password,UserUtil.getUserAuthentication().getPassword())){//the userUtil is admin user
            List<QuestionPaper> result=new ArrayList<>();
            for (QuestionPaper questionPaper:questionPaperRepository.findByGeneratedBy(
                    userService.findUserById(
                            originalID
                    )
            )){
                questionPaper.setApprovedBy(replaceUser);
                result.add(questionPaper);
            }
            return questionPaperRepository.saveAll(result);
        }
        else {
            throw new WrongPasswordException("your password is wrong");
        }
    }

    @SneakyThrows
    @Transactional
    @PreAuthorize("ROLE_TEACHER")
    public QuestionPaper addQuestionPaper(QuestionPaper questionPaper){
        List<Long>Ids=questionPaper.getListOfQuestion()
                .stream()
                .sorted(Comparator.comparing(Question::getId))
                .map(Question::getId)
                .toList();
        String questionPaperFingerprint;
        Set<Question> questions=new HashSet<>(questionService.getQuestionByIds(Ids));
        if (!(Ids.size()==questions.size())){
            throw new BadRequestException("there are a few band questions");
        }
        else {
            questionPaperFingerprint = QuestionPaperUtil.sha256FingerPrintUsingIds(Ids);
            if (questionPaperRepository.existsByQuestionPaperFingerprint(questionPaperFingerprint)) {
                throw new DuplicateQuestionPaperException("one more question paper with exact questions exists");
            } else {
                questionPaper.setListOfQuestion(questions);
                questionPaper.setGeneratedBy(UserUtil.getUserAuthentication().getUser());
                questionPaper.setApproved(Boolean.FALSE);
                questionPaper.setQuestionPaperFingerprint(questionPaperFingerprint);
                questionPaperRepository.save(questionPaper);
            }
        }
        return questionPaper;
    }
}