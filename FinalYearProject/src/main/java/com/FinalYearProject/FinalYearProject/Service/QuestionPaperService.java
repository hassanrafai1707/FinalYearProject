package com.FinalYearProject.FinalYearProject.Service;

import com.FinalYearProject.FinalYearProject.DTO.QuestionDto.QuestionDTO;
import com.FinalYearProject.FinalYearProject.Domain.Question;
import com.FinalYearProject.FinalYearProject.Domain.QuestionPaper;
import com.FinalYearProject.FinalYearProject.Domain.User;
import com.FinalYearProject.FinalYearProject.Exceptions.DepartmentMissMatchException;
import com.FinalYearProject.FinalYearProject.Exceptions.QuestionException.QuestionNotFoundException;
import com.FinalYearProject.FinalYearProject.Exceptions.QuestionPaperException.DuplicateQuestionPaperException;
import com.FinalYearProject.FinalYearProject.Exceptions.QuestionPaperException.QuestionPaperNotFoundException;
import com.FinalYearProject.FinalYearProject.Exceptions.UserEeceptions.UserNotAuthorizesException;
import com.FinalYearProject.FinalYearProject.Exceptions.UserEeceptions.WrongPasswordException;
import com.FinalYearProject.FinalYearProject.Repository.QuestionPaperRepository;
import com.FinalYearProject.FinalYearProject.Util.QuestionPaperUtil;
import com.FinalYearProject.FinalYearProject.Util.UserUtil;
import lombok.SneakyThrows;
import org.apache.coyote.BadRequestException;
import org.openpdf.text.*;
import org.openpdf.text.Font;
import org.openpdf.text.pdf.PdfPCell;
import org.openpdf.text.pdf.PdfPTable;
import org.openpdf.text.pdf.PdfTable;
import org.openpdf.text.pdf.PdfWriter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.awt.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.*;
import java.util.List;
import java.util.stream.Stream;

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
        QuestionPaper questionPaper=findById(id);
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
        QuestionPaper questionPaper=findById(id);
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

    @PreAuthorize("hasRole('TEACHER')")
    public Page<QuestionPaper> myQuestionPapers(int pageNo,int size){
        User user = UserUtil.getUserAuthentication().getUser();
        Page<QuestionPaper> questionPapersGeneratedByUser=questionPaperRepository.findByGeneratedBy(
                user,
                user.getDepartment(),
                PageRequest.of(pageNo,size)
        );
        if (!(questionPapersGeneratedByUser.isEmpty())){
            return questionPapersGeneratedByUser;
        }
        else {
            throw new QuestionPaperNotFoundException("no question paper has been generated by user with email "+user.getEmail());
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

    @SneakyThrows
    public ByteArrayInputStream downloadQuestionPaper(Long id){
        QuestionPaper questionPaper=findById(id);
        List<Question> questionList=questionPaper.getListOfQuestion().stream().toList();
        if (!questionPaper.getGeneratedBy().getDepartment().equals(UserUtil.getUserAuthentication().getUser().getDepartment())){
            throw new DepartmentMissMatchException("you can not download this question ");
        }
        if (!questionPaper.getApproved()) {
            throw new BadRequestException("you can only download Approved question paper");
        }
        Document document=new Document();
        ByteArrayOutputStream outputStream=new ByteArrayOutputStream();
        try {
            PdfWriter.getInstance(document,outputStream);
            document.open();

            // Fonts
            Font titleFont = FontFactory.getFont(FontFactory.TIMES_BOLD,22);
            Font headerFont = FontFactory.getFont(FontFactory.TIMES_BOLD,12);
            Font bodyFont = FontFactory.getFont(FontFactory.TIMES_BOLD,10);

            // Title
            Paragraph paragraph=new Paragraph(questionPaper.getExamTitle(),titleFont );
            paragraph.setAlignment(Element.ALIGN_CENTER);
            document.add(paragraph);
            document.add(Chunk.NEWLINE);

            // Metadata
            document.addTitle(questionPaper.getExamTitle());
            document.addAuthor(questionPaper.getGeneratedBy().getEmail());
            document.addSubject("Question Paper");
            document.addCreationDate();

            // Page numbers
            HeaderFooter footer = new HeaderFooter(new Phrase("Page "), new Phrase(""));
            footer.setAlignment(Element.ALIGN_CENTER);
            document.setFooter(footer);

            // Create table
            PdfPTable pdfTable=new PdfPTable(5);
            float[] columnWidths = {0.5f, 0.8f, 0.8f, 4f, 0.6f};
            pdfTable.setWidths(columnWidths);
            pdfTable.setHeaderRows(1);

            Stream.of("ID", "Cognitive Level","CO","Question" ,"Marks").forEach(
                    hederTitle->{
                        PdfPCell heder=new PdfPCell();
                        heder.setBackgroundColor(Color.green);
                        heder.setHorizontalAlignment(Element.ALIGN_CENTER);
                        heder.setBorderWidth(2);
                        heder.setPhrase(new Phrase(hederTitle,headerFont ));
                        pdfTable.addCell(heder);
                    }
            );
            if (questionList.isEmpty()) throw new QuestionNotFoundException("there are not questions in this question paper");

            for (Question question : questionList.stream().sorted(Comparator.comparing(Question::getQuestionMarks)).toList()){

                // ID
                PdfPCell qId=new PdfPCell(new Phrase(question.getId().toString(),bodyFont));
                qId.setPadding(4);
                qId.setVerticalAlignment(Element.ALIGN_MIDDLE);
                qId.setHorizontalAlignment(Element.ALIGN_CENTER);
                pdfTable.addCell(qId);

                // Cognitive Level
                PdfPCell Cognitive=new PdfPCell(new Phrase(question.getCognitiveLevel(),bodyFont));
                Cognitive.setPadding(4);
                Cognitive.setVerticalAlignment(Element.ALIGN_MIDDLE);
                Cognitive.setHorizontalAlignment(Element.ALIGN_CENTER);
                pdfTable.addCell(Cognitive);

                // CO (Mapped CO)
                PdfPCell CO=new PdfPCell(new Phrase(question.getMappedCO(),bodyFont));
                CO.setPadding(4);
                CO.setVerticalAlignment(Element.ALIGN_MIDDLE);
                CO.setHorizontalAlignment(Element.ALIGN_CENTER);
                pdfTable.addCell(CO);

                // Question Body (with wrapping)
                PdfPCell Question=new PdfPCell(new Phrase(question.getQuestionBody(),bodyFont));
                Question.setPadding(4);
                Question.setVerticalAlignment(Element.ALIGN_TOP);
                Question.setHorizontalAlignment(Element.ALIGN_LEFT);
                Question.setNoWrap(false);
                pdfTable.addCell(Question);

                // Marks
                PdfPCell Marks=new PdfPCell(new Phrase(String.valueOf(question.getQuestionMarks()),bodyFont));
                Marks.setPadding(4);
                Marks.setVerticalAlignment(Element.ALIGN_MIDDLE);
                Marks.setHorizontalAlignment(Element.ALIGN_CENTER);
                pdfTable.addCell(Marks);
            }

            // Total marks row
            PdfPCell totalLabel = new PdfPCell(new Phrase("TOTAL MARKS:", bodyFont));
            totalLabel.setColspan(4);
            totalLabel.setHorizontalAlignment(Element.ALIGN_RIGHT);
            totalLabel.setBackgroundColor(Color.LIGHT_GRAY);
            totalLabel.setPadding(5);
            pdfTable.addCell(totalLabel);

            PdfPCell totalValue =
                    new PdfPCell(
                    new Phrase(
                            String.valueOf(
                                    questionList.stream().mapToInt(Question::getQuestionMarks).sum()
                            ),
                            bodyFont
                    )
                    );
            totalValue.setHorizontalAlignment(Element.ALIGN_CENTER);
            totalValue.setBackgroundColor(Color.LIGHT_GRAY);
            totalValue.setPadding(5);
            pdfTable.addCell(totalValue);

            document.add(pdfTable);
        }
        catch (DocumentException e){
            throw new RuntimeException(e);//don't change this line will give error code 500
        }
        finally {
            if (document != null && document.isOpen()) {
                document.close();
            }
        }
        return new ByteArrayInputStream(outputStream.toByteArray());
    }

    @SneakyThrows
    public ByteArrayInputStream downloadQuestionPaperTeacher(Long id){
        QuestionPaper questionPaper=findById(id);
        List<Question> questionList=questionPaper.getListOfQuestion().stream().toList();
        if (questionPaper.getGeneratedBy().getId()!=UserUtil.getUserAuthentication().getUser().getId()){
            throw new UserNotAuthorizesException("You are not aloud to download this question paper ");
        }
        if (!questionPaper.getApproved()) {
            throw new BadRequestException("you can only download Approved question paper");
        }
        Document document=new Document();
        ByteArrayOutputStream outputStream=new ByteArrayOutputStream();
        try {
            PdfWriter.getInstance(document,outputStream);
            document.open();

            // Fonts
            Font titleFont = FontFactory.getFont(FontFactory.TIMES_BOLD,22);
            Font headerFont = FontFactory.getFont(FontFactory.TIMES_BOLD,12);
            Font bodyFont = FontFactory.getFont(FontFactory.TIMES_BOLD,10);

            // Title
            Paragraph paragraph=new Paragraph(questionPaper.getExamTitle(),titleFont );
            paragraph.setAlignment(Element.ALIGN_CENTER);
            document.add(paragraph);
            document.add(Chunk.NEWLINE);

            // Metadata
            document.addTitle(questionPaper.getExamTitle());
            document.addAuthor(questionPaper.getGeneratedBy().getEmail());
            document.addSubject("Question Paper");
            document.addCreationDate();

            // Page numbers
            HeaderFooter footer = new HeaderFooter(new Phrase("Page "), new Phrase(""));
            footer.setAlignment(Element.ALIGN_CENTER);
            document.setFooter(footer);

            // Create table
            PdfPTable pdfTable=new PdfPTable(5);
            float[] columnWidths = {0.5f, 0.8f, 0.8f, 4f, 0.6f};
            pdfTable.setWidths(columnWidths);
            pdfTable.setHeaderRows(1);

            Stream.of("ID", "Cognitive Level","CO","Question" ,"Marks").forEach(
                    hederTitle->{
                        PdfPCell heder=new PdfPCell();
                        heder.setBackgroundColor(Color.green);
                        heder.setHorizontalAlignment(Element.ALIGN_CENTER);
                        heder.setBorderWidth(2);
                        heder.setPhrase(new Phrase(hederTitle,headerFont ));
                        pdfTable.addCell(heder);
                    }
            );
            if (questionList.isEmpty()) throw new QuestionNotFoundException("there are not questions in this question paper");

            for (Question question : questionList.stream().sorted(Comparator.comparing(Question::getQuestionMarks)).toList()){

                // ID
                PdfPCell qId=new PdfPCell(new Phrase(question.getId().toString(),bodyFont));
                qId.setPadding(4);
                qId.setVerticalAlignment(Element.ALIGN_MIDDLE);
                qId.setHorizontalAlignment(Element.ALIGN_CENTER);
                pdfTable.addCell(qId);

                // Cognitive Level
                PdfPCell Cognitive=new PdfPCell(new Phrase(question.getCognitiveLevel(),bodyFont));
                Cognitive.setPadding(4);
                Cognitive.setVerticalAlignment(Element.ALIGN_MIDDLE);
                Cognitive.setHorizontalAlignment(Element.ALIGN_CENTER);
                pdfTable.addCell(Cognitive);

                // CO (Mapped CO)
                PdfPCell CO=new PdfPCell(new Phrase(question.getMappedCO(),bodyFont));
                CO.setPadding(4);
                CO.setVerticalAlignment(Element.ALIGN_MIDDLE);
                CO.setHorizontalAlignment(Element.ALIGN_CENTER);
                pdfTable.addCell(CO);

                // Question Body (with wrapping)
                PdfPCell Question=new PdfPCell(new Phrase(question.getQuestionBody(),bodyFont));
                Question.setPadding(4);
                Question.setVerticalAlignment(Element.ALIGN_TOP);
                Question.setHorizontalAlignment(Element.ALIGN_LEFT);
                Question.setNoWrap(false);
                pdfTable.addCell(Question);

                // Marks
                PdfPCell Marks=new PdfPCell(new Phrase(String.valueOf(question.getQuestionMarks()),bodyFont));
                Marks.setPadding(4);
                Marks.setVerticalAlignment(Element.ALIGN_MIDDLE);
                Marks.setHorizontalAlignment(Element.ALIGN_CENTER);
                pdfTable.addCell(Marks);
            }

            // Total marks row
            PdfPCell totalLabel = new PdfPCell(new Phrase("TOTAL MARKS:", bodyFont));
            totalLabel.setColspan(4);
            totalLabel.setHorizontalAlignment(Element.ALIGN_RIGHT);
            totalLabel.setBackgroundColor(Color.LIGHT_GRAY);
            totalLabel.setPadding(5);
            pdfTable.addCell(totalLabel);

            PdfPCell totalValue =
                    new PdfPCell(
                            new Phrase(
                                    String.valueOf(
                                            questionList.stream().mapToInt(Question::getQuestionMarks).sum()
                                    ),
                                    bodyFont
                            )
                    );
            totalValue.setHorizontalAlignment(Element.ALIGN_CENTER);
            totalValue.setBackgroundColor(Color.LIGHT_GRAY);
            totalValue.setPadding(5);
            pdfTable.addCell(totalValue);

            document.add(pdfTable);
        }
        catch (DocumentException e){
            throw new RuntimeException(e);//don't change this line will give error code 500
        }
        finally {
            if (document != null && document.isOpen()) {
                document.close();
            }
        }
        return new ByteArrayInputStream(outputStream.toByteArray());
    }
}