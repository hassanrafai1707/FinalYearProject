package com.FinalYearProject.FinalYearProject.Service;

import com.FinalYearProject.FinalYearProject.DTO.QuestionDto.QuestionDTO;
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
        this.questionPaperRepository=questionPaperRepository;
        this.userService=userService;
        this.questionService=questionService;
    }

    @PreAuthorize("hasRole('SUPERVISOR')")
    public List<QuestionPaper> getAllQuestionPapers(){
        List<QuestionPaper> questionPapers=questionPaperRepository.findByDepartment(UserUtil.getUserAuthentication().getUser().getDepartment());
        if (questionPapers.isEmpty()){
            throw new QuestionPaperNotFoundException("no question paper in db");
        }
        else {
            return questionPapers;
        }
    }

    @PreAuthorize("hasRole('SUPERVISOR')")
    public Page<QuestionPaper> getAllQuestionPapers(int pageNo , int size){
        Page<QuestionPaper> questionPaperPage=questionPaperRepository.findByDepartment(
                UserUtil.getUserAuthentication().getUser().getDepartment(),
                PageRequest.of(pageNo,size)
        );
        if (!(questionPaperPage.isEmpty())){
            return questionPaperPage;
        }
        else {
            throw new QuestionPaperNotFoundException("no question paper in db ");
        }
    }

    @PreAuthorize("hasRole('SUPERVISOR')")
    public QuestionPaper findById(Long Id){
        return questionPaperRepository.findById(Id,UserUtil.getUserAuthentication().getUser().getDepartment())
                .orElseThrow(()-> new QuestionPaperNotFoundException("no question paper with id"+Id+" in your determent "));
    }

    @PreAuthorize("hasRole('SUPERVISOR')")
    public QuestionPaper findByExamTitle(String examTitle) {
        return questionPaperRepository.findByExamTitle(examTitle,UserUtil.getUserAuthentication().getUser().getDepartment())
                .orElseThrow(()-> new QuestionPaperNotFoundException("no question paper with exam title"+examTitle));
    }

    @PreAuthorize("hasRole('SUPERVISOR')")
    public List<QuestionPaper> findByGeneratedByUsingEmail(String email){
        List<QuestionPaper> questionPapersGeneratedByUser=questionPaperRepository.findByGeneratedBy(
                userService.findByEmail(email),
                UserUtil.getUserAuthentication().getUser().getDepartment()
        );
        if (questionPapersGeneratedByUser.isEmpty()){
            throw new QuestionPaperNotFoundException("no question paper has been generated by user with email "+email);
        }
        else {
            return questionPapersGeneratedByUser;
        }
    }

    @PreAuthorize("hasRole('SUPERVISOR')")
    public Page<QuestionPaper> findByGeneratedByUsingEmail(String email, int pageNo, int size){
        Page<QuestionPaper> questionPapersGeneratedByUser=questionPaperRepository.findByGeneratedBy(
                userService.findByEmail(email),
                UserUtil.getUserAuthentication().getUser().getDepartment(),
                PageRequest.of(pageNo,size)
        );
        if (!(questionPapersGeneratedByUser.isEmpty())){
            return questionPapersGeneratedByUser;
        }
        else {
            throw new QuestionPaperNotFoundException("no question paper has been generated by user with email "+email);
        }
    }

    @PreAuthorize("hasRole('SUPERVISOR')")
    public List<QuestionPaper> findByGeneratedByUsingId(Long Id){
        List<QuestionPaper> questionPapers=questionPaperRepository.findByGeneratedBy(
                userService.findUserById(Id),
                UserUtil.getUserAuthentication().getUser().getDepartment()
        );
        if (questionPapers.isEmpty()){
            throw new QuestionPaperNotFoundException("no question paper has been generated by user with Id "+Id);
        }
        else {
            return questionPapers;
        }
    }

    @PreAuthorize("hasRole('SUPERVISOR')")
    public Page<QuestionPaper> findByGeneratedByUsingId(Long Id,int pageNo,int size){
        Page<QuestionPaper> questionPapers=questionPaperRepository.findByGeneratedBy(
                userService.findUserById(Id),
                UserUtil.getUserAuthentication().getUser().getDepartment(),
                PageRequest.of(pageNo,size)
        );
        if (!(questionPapers.isEmpty())){
            return questionPapers;
        }
        else {
            throw new QuestionPaperNotFoundException("no question paper has been generated by user with Id "+Id);
        }
    }

    @PreAuthorize("hasRole('SUPERVISOR')")
    public List<QuestionPaper> findByApprovedByUsingEmail(String email){
        List<QuestionPaper> questionPapers=questionPaperRepository.findByApprovedBy(
                userService.findByEmail(email),
                UserUtil.getUserAuthentication().getUser().getDepartment()
        );
        if (questionPapers.isEmpty()){
            throw new QuestionPaperNotFoundException("no question paper has been generated by user with email "+email);
        }
        else {
            return questionPapers;
        }
    }

    @PreAuthorize("hasRole('SUPERVISOR')")
    public  Page<QuestionPaper> findByApprovedByUsingEmail(String email, int pageNo , int size){
        Page<QuestionPaper> questionPapers=questionPaperRepository.findByApprovedBy(
                userService.findByEmail(email),
                UserUtil.getUserAuthentication().getUser().getDepartment(),
                PageRequest.of(pageNo,size)
        );
        if (!(questionPapers.isEmpty())){
            return questionPapers;
        }
        else {
            throw new QuestionPaperNotFoundException("no question paper has been generated by user with email "+email);

        }
    }

    @PreAuthorize("hasRole('SUPERVISOR')")
    public List<QuestionPaper> findByApprovedByUsingId(Long Id){
        List<QuestionPaper> questionPapers=questionPaperRepository.findByApprovedBy(
                userService.findUserById(Id),
                UserUtil.getUserAuthentication().getUser().getDepartment()
        );
        if (questionPapers.isEmpty()){
            throw new QuestionPaperNotFoundException("no question paper has been generated by user with Id "+Id);
        }
        else {
            return questionPapers;
        }
    }

    @PreAuthorize("hasRole('SUPERVISOR')")
    public Page<QuestionPaper> findByApprovedByUsingId(Long Id,int pageNo,int size){
        Page<QuestionPaper> questionPapers=questionPaperRepository.findByApprovedBy(
                userService.findUserById(Id),
                UserUtil.getUserAuthentication().getUser().getDepartment(),
                PageRequest.of(pageNo,size)
        );
        if (!(questionPapers.isEmpty())){
            return questionPapers;
        }
        else {
            throw new QuestionPaperNotFoundException("no question paper has been generated by user with Id "+Id);
        }
    }

    @PreAuthorize("hasRole('SUPERVISOR')")
    public List<QuestionPaper> findApproved(){
        List<QuestionPaper> questionPapers=questionPaperRepository.findByApproved(Boolean.TRUE,UserUtil.getUserAuthentication().getUser().getDepartment());
        if (questionPapers.isEmpty()){
            throw new QuestionPaperNotFoundException("no question paper has been approved yet ");
        }
        else {
            return questionPapers;
        }
    }

    @PreAuthorize("hasRole('SUPERVISOR')")
    public Page<QuestionPaper> findApproved(int pageNo,int size){
        Page<QuestionPaper> questionPapers=questionPaperRepository.findByApproved(
                Boolean.TRUE,
                UserUtil.getUserAuthentication().getUser().getDepartment(),
                PageRequest.of(pageNo,size)
        );
        if (!(questionPapers.isEmpty())){
            return questionPapers;
        }
        else {
            throw new QuestionPaperNotFoundException("no question paper has been approved yet ");
        }
    }

    @PreAuthorize("hasRole('SUPERVISOR')")
    public List<QuestionPaper> findNotApproved(){
        List<QuestionPaper> questionPapers=questionPaperRepository.findByApproved(Boolean.FALSE,UserUtil.getUserAuthentication().getUser().getDepartment());
        if (questionPapers.isEmpty()){
            throw new QuestionPaperNotFoundException("no question paper has been left to approve");
        }
        else {
            return questionPapers;
        }
    }

    @PreAuthorize("hasRole('SUPERVISOR')")
    public Page<QuestionPaper> findNotApproved(int pageNo,int size){
        Page<QuestionPaper> questionPapers=questionPaperRepository.findByApproved(
                Boolean.FALSE,UserUtil.getUserAuthentication().getUser().getDepartment(),PageRequest.of(pageNo,size)
        );
        if (!(questionPapers.isEmpty())){
            return questionPapers;
        }
        else {
            throw new QuestionPaperNotFoundException("no question paper has been approved yet ");
        }
    }

    @Transactional
    @PreAuthorize("hasRole('SUPERVISOR')")
    public QuestionPaper approveQuestionPaperById(Long id,String comment){
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
            questionPaper.setComment(comment);
            questionPaperRepository.save(questionPaper);
            return questionPaper;
        }
    }

    @Transactional
    @PreAuthorize("hasRole('SUPERVISOR')")
    public QuestionPaper notApproveQuestionPaperById(Long id,String comment){
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
            questionPaper.setComment(comment);
            questionPaperRepository.save(questionPaper);
            return questionPaper;
        }
    }

    @Transactional
    @PreAuthorize("hasRole('SUPERVISOR')")
    public QuestionPaper approvedQuestionPaperByTile(String examTitle,String comment){
        QuestionPaper questionPaper = findByExamTitle(examTitle);
        if (questionPaper.getApproved().equals(Boolean.TRUE)){
            return questionPaper;
        }
        else {
            questionPaper.setApproved(Boolean.TRUE);
            questionPaper.setApprovedBy(UserUtil.getUserAuthentication().getUser());
            questionPaper.setComment(comment);
            questionPaperRepository.save(questionPaper);
            return questionPaper;
        }
    }

    @Transactional
    @PreAuthorize("hasRole('SUPERVISOR')")
    public QuestionPaper notApprovedQuestionPaperByTile(String examTitle,String comment){
        QuestionPaper questionPaper = findByExamTitle(examTitle);
        if (questionPaper.getApproved().equals(Boolean.FALSE)){
            return questionPaper;
        }
        else {
            questionPaper.setApproved(Boolean.FALSE);
            questionPaper.setApprovedBy(UserUtil.getUserAuthentication().getUser());
            questionPaper.setComment(comment);
            questionPaperRepository.save(questionPaper);
            return questionPaper;
        }
    }

    @SneakyThrows
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public void updateGeneratedByUsingEmail(String replaceEmail,String originalEmail,String password){
        if(
                replaceEmail.isEmpty()||
                        originalEmail.isEmpty()||
                        password.isEmpty()||
                        !(
                                userService.existsByEmail(replaceEmail)&&
                                        userService.existsByEmail(originalEmail)
                        )
        ){
            throw new BadRequestException("this request is invalid because one of the given parameter is empty");
        }
        User replaceUser =userService.findByEmail(replaceEmail);
        if (!replaceUser.getRole().contains("ROLE_TEACHER")){
            throw new UserNotAuthorizesException("user with email:"+ replaceUser.getEmail()+"has role:"+ replaceUser.getRole()+" and not role ROLE_TEACHER");
        }
        if(userService.matchPasswords(password,UserUtil.getUserAuthentication().getPassword())){//the userutil is admin user
            List<QuestionPaper> result=new ArrayList<>();
            for (QuestionPaper questionPaper:findByGeneratedByUsingEmail(
                    originalEmail
            )){
                questionPaper.setGeneratedBy(replaceUser);
                result.add(questionPaper);
            }
            questionPaperRepository.saveAll(result);
        }
        else {
            throw new WrongPasswordException("your password is wrong");
        }
    }

    @SneakyThrows
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public void updateGeneratedByUsingId(Long replaceID,Long originalID,String password){
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
            List<QuestionPaper> result=new ArrayList<>();
            for (QuestionPaper questionPaper:findByGeneratedByUsingId(
                    originalID
            )){
                questionPaper.setGeneratedBy(replaceUser);
                result.add(questionPaper);
            }
            questionPaperRepository.saveAll(result);
        }
        else {
            throw new WrongPasswordException("your password is wrong");
        }
    }

    @SneakyThrows
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public void updateApprovedByUsingEmail(String replaceEmail,String originalEmail,String password){
        if(
                replaceEmail.isEmpty()||
                        originalEmail.isEmpty()||
                        password.isEmpty()||
                        !(
                                userService.existsByEmail(replaceEmail)&&
                                        userService.existsByEmail(originalEmail)
                        )
        ){
            throw new BadRequestException("this request is invalid because one of the given parameter is empty");
        }
        User replaceUser =userService.findByEmail(replaceEmail);
        if (!replaceUser.getRole().contains("ROLE_TEACHER")){
            throw new UserNotAuthorizesException("user with email:"+ replaceUser.getEmail()+"has role:"+ replaceUser.getRole()+" and not role ROLE_TEACHER");
        }
        if(userService.matchPasswords(password,UserUtil.getUserAuthentication().getPassword())){//the userutil is admin user
            List<QuestionPaper> result=new ArrayList<>();
            for (QuestionPaper questionPaper:findByGeneratedByUsingEmail(
                    originalEmail
            )){
                questionPaper.setApprovedBy(replaceUser);
                result.add(questionPaper);
            }
            questionPaperRepository.saveAll(result);
        }
        else {
            throw new WrongPasswordException("your password is wrong");
        }
    }

    @SneakyThrows
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public void updateApprovedByUsingId(Long replaceID,Long originalID,String password){

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
            List<QuestionPaper> result=new ArrayList<>();
            for (QuestionPaper questionPaper:findByGeneratedByUsingId(
                    originalID
            )){
                questionPaper.setApprovedBy(replaceUser);
                result.add(questionPaper);
            }
            questionPaperRepository.saveAll(result);
        }
        else {
            throw new WrongPasswordException("your password is wrong");
        }
    }

    @SneakyThrows
    @Transactional
    @PreAuthorize("hasRole('TEACHER')")
    public QuestionPaper addQuestionPaper(List<QuestionDTO> questions, String examTitle) {
        List<Long> ids = questions.stream()
                .sorted(Comparator.comparing(QuestionDTO::getId))
                .map(QuestionDTO::getId)
                .toList();
        List<Long> validIds = questionService.validIDS(ids);
        if (questionPaperRepository.existsByExamTitle(examTitle)){
            throw new DuplicateQuestionPaperException("Question paper already exists with this examTitle: "+examTitle);
        }
        if (validIds.size() != ids.size()) {
            List<Long> invalidIds = new ArrayList<>(ids);
            invalidIds.removeAll(validIds);
            throw new BadRequestException("Invalid question IDs: " + invalidIds);
        }
        String fingerprint = QuestionPaperUtil.sha256FingerPrintUsingIds(validIds);
        if (questionPaperRepository.existsByQuestionPaperFingerprint(fingerprint)) {
            throw new DuplicateQuestionPaperException("Question paper already exists");
        }
        QuestionPaper questionPaper = new QuestionPaper();
        questionPaper.setListOfQuestion(new HashSet<>(questionService.getQuestionByIDS(validIds)));
        questionPaper.setGeneratedBy(UserUtil.getUserAuthentication().getUser());
        questionPaper.setApproved(false);
        questionPaper.setQuestionPaperFingerprint(fingerprint);
        questionPaper.setExamTitle(examTitle);
        return questionPaperRepository.save(questionPaper);
    }
}