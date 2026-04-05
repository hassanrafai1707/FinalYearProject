package com.FinalYearProject.FinalYearProject.Controller;

import com.FinalYearProject.FinalYearProject.DTO.QuestionPaperDto.ExamTitleAndComment;
import com.FinalYearProject.FinalYearProject.DTO.QuestionPaperDto.IdAndComment;
import com.FinalYearProject.FinalYearProject.Service.QuestionPaperService;
import com.FinalYearProject.FinalYearProject.Service.QuestionService;
import com.FinalYearProject.FinalYearProject.Service.UserService;
import com.FinalYearProject.FinalYearProject.Util.QuestionDtoUtil;
import com.FinalYearProject.FinalYearProject.Util.QuestionPaperDtoUtil;
import com.FinalYearProject.FinalYearProject.Util.ResponseUtility;
import com.FinalYearProject.FinalYearProject.Util.UserDtoUtil;
import lombok.SneakyThrows;
import org.apache.coyote.BadRequestException;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayInputStream;
import java.util.Map;

/**
 * Supervisor REST Controller for Question Paper Review and Approval
 * PURPOSE: Provides comprehensive question paper management and approval system for supervisors. Requires ROLE_SUPERVISOR or ROLE_ADMIN authority. Combines question retrieval, question paper review, and approval workflows.
 * QUESTION PAPER MANAGEMENT OPERATIONS:
 * 1. QUESTION PAPER RETRIEVAL: getAllQuestionsPaper returns all question papers. getAllQuestionsPaperPaged provides paginated results. getQuestionPaperById retrieves single paper.
 * 2. FILTERING BY ATTRIBUTES: findByExamTitle filters by exam title. findByGeneratedByUsingEmail/Id finds papers created by specific users (with pagination). findByApprovedByUsingEmail/Id finds papers approved by specific supervisors (with pagination).
 * 3. APPROVAL STATUS FILTERS: findApproved returns only approved papers. findNotApproved returns pending/rejected papers. Both support pagination.
 * 4. APPROVAL WORKFLOWS: approveQuestionPaperById/ByTile approves papers for use. notApproveQuestionPaperById/ByTile rejects or returns papers for revision.
 * QUESTION BANK ACCESS (SIMILAR TO STUDENT): getAllQuestion, getQuestionById, findBySubjectCode, findBySubjectName with all filtering variants (MappedCO, CognitiveLevel). Includes pagination support.
 * CREATOR TRACKING: findByCreatedByUsingEmail/Id retrieves questions created by specific teachers for quality review.
 * SUPERVISOR SELF-SERVICE: updateUserEmail and updateUserPassword for account management. test endpoint for connectivity verification.
 * SECURITY CONTEXT: Requires supervisor or admin role. Supervisors can approve/reject papers created by teachers. Access to full question bank for review purposes. Can view approval history by supervisor.
 * WORKFLOW MANAGEMENT: Tracks paper status (approved/not approved). Maintains audit trail of who approved each paper. Enables quality control process before exam deployment.
 * RESPONSE FORMAT: Consistent JSON structure: {"status": "Successful", "dataKey": data}. Paginated endpoints return PagedModel with page metadata.
 * AUDIT TRAIL FEATURES: Tracks generatedBy (creator) and approvedBy (supervisor). Timestamps for creation and approval. Status history for papers.
 */
@RequestMapping("${app.version}/supervisor")
@RestController
public class SupervisorRestController {
    private final UserService userService;
    private final QuestionService questionService;
    private final QuestionPaperService questionPaperService;

    public SupervisorRestController(UserService userService,QuestionService questionService,QuestionPaperService questionPaperService){
       this.userService=userService;
       this.questionService=questionService;
       this.questionPaperService=questionPaperService;
    }

    @GetMapping("/questions")
    public ResponseEntity<?> getAllQuestion(){
        return ResponseUtility.responseTemplateForMultipleData(
                "successful",
                QuestionDtoUtil.listOfQuestionToQuestionDto(
                        questionService.getAllQuestion()
                ).toArray(),
                "All questions",
                200
        );
    }

    @GetMapping("/questions/paged")
    public ResponseEntity<?>  getAllQuestionsPaged(
            @RequestParam(value = "pageNo", defaultValue = "0") int pageNo,
            @RequestParam(value = "size",defaultValue = "100") int size
    ){
        return ResponseUtility.responseTemplateForSingleData(
                "successful",
                QuestionDtoUtil.questionToQuestionDTO_Paged(
                        questionService.getAllQuestionsPaged(pageNo, size)
                ),
                "All questions in the given page",
                200
        );
    }

    @GetMapping("/question/id")
    @SneakyThrows
    public ResponseEntity<?> getQuestionById (@RequestParam("id") Long id){
        if (id.toString().isEmpty()){
            throw new BadRequestException("the request must contain 'id'");
        }
        return ResponseUtility.responseTemplateForSingleData(
                "successful",
                QuestionDtoUtil.QuestionToQuestionDto(
                        questionService.getQuestionById(id)
                ),
                "question with id "+id,
                200
        );
    }

    @GetMapping("/questions/subjectCode")
    @SneakyThrows
    public ResponseEntity<?> findBySubjectCode(@RequestParam("subjectCode") String subjectCode) {
        if (subjectCode.isEmpty()){
            throw new BadRequestException("the request must contain 'subjectCode'");
        }
        return ResponseUtility.responseTemplateForMultipleData(
                "successful",
                QuestionDtoUtil.listOfQuestionToQuestionDto(
                        questionService.findBySubjectCode(subjectCode)
                ).toArray(),
                "questions with subject code "+subjectCode,
                200
        );
    }

    @GetMapping("/questions/subjectCode/pagged")
    @SneakyThrows
    public ResponseEntity<?> findBySubjectCode(
            @RequestParam(value = "pageNo" ,defaultValue = "0") int pageNo,
            @RequestParam(value = "size" , defaultValue = "100") int size,
            @RequestParam("subjectCode") String subjectCode
    ){
        if (subjectCode.isEmpty()){
            throw new BadRequestException("the request must contain 'subjectCode'");
        }
        return ResponseUtility.responseTemplateForSingleData(
                "successful",
                QuestionDtoUtil.questionToQuestionDTO_Paged(
                        questionService.findBySubjectCode(
                                subjectCode,
                                pageNo,
                                size
                        )
                ),
                "All questions in the given page",
                200
        );
    }

    @GetMapping("/questions/subjectCode/mappedCO")
    @SneakyThrows
    public ResponseEntity<?> findBySubjectCodeMappedCO(
            @RequestParam("subjectCode") String subjectCode,
            @RequestParam("mappedCO") String mappedCO
    ){
        if (
                subjectCode.isEmpty()
                        ||mappedCO.isEmpty()
        ){
            throw new BadRequestException("the request must contain 'subjectCode' and 'mappedCO'");
        }
        return ResponseUtility.responseTemplateForMultipleData(
                "successful",
                questionService.findBySubjectCodeMappedCO(
                        subjectCode,
                        mappedCO
                ).toArray(),
                "questions with subject code and mapped CO "+subjectCode+" "+mappedCO,
                200
        );
    }

    @GetMapping("/questions/subjectCode/mappedCO/pagged")
    @SneakyThrows
    public ResponseEntity<?> findBySubjectCodeMappedCO(
            @RequestParam(value = "pageNo",defaultValue = "0")int pageNo,
            @RequestParam(value = "size",defaultValue = "100")int size,
            @RequestParam("subjectCode") String subjectCode,
            @RequestParam("mappedCO") String mappedCO
    ){
        if (
                subjectCode.isEmpty()
                        ||mappedCO.isEmpty()
        ){
            throw new BadRequestException("the request must contain 'subjectCode' and 'mappedCO'");
        }
        return ResponseUtility.responseTemplateForSingleData(
                "successful",
                QuestionDtoUtil.questionToQuestionDTO_Paged(
                        questionService.findBySubjectCodeMappedCO(
                                subjectCode,
                                mappedCO,
                                pageNo,
                                size
                        )
                ),
                "All questions in the given page",
                200
        );
    }

    @GetMapping("/questions/subjectCode/mappedCO/cognitiveLevel")
    @SneakyThrows
    public ResponseEntity<?> findBySubjectCodeMappedCOCognitiveLevel(
            @RequestParam("subjectCode") String subjectCode,
            @RequestParam("mappedCO") String mappedCO,
            @RequestParam("cognitiveLevel")String cognitiveLevel
    ){
        if (
                subjectCode.isEmpty()
                        ||mappedCO.isEmpty()
                        ||cognitiveLevel.isEmpty()
        ){
            throw new BadRequestException("the request must contain 'subjectCode' and 'mappedCO' and 'cognitiveLevel'");
        }
        return ResponseUtility.responseTemplateForSingleData(
                "successful",
                QuestionDtoUtil.listOfQuestionToQuestionDto(
                        questionService.findBySubjectCodeMappedCOCognitiveLevel(
                                subjectCode,
                                mappedCO,
                                cognitiveLevel
                        )
                ),
                "All questions with selected subject code, mapped co and cognitiveLevel ",
                200
        );
    }

    @GetMapping("/questions/subjectCode/mappedCO/cognitiveLevel/pagged")
    @SneakyThrows
    private ResponseEntity<?> findByCognitiveLevel(
            @RequestParam(value = "pageNo",defaultValue = "0")int pageNo,
            @RequestParam(value = "size",defaultValue = "100")int size,
            @RequestParam("subjectCode") String subjectCode,
            @RequestParam("mappedCO") String mappedCO,
            @RequestParam("cognitiveLevel")String cognitiveLevel
    ){
        if (
                subjectCode.isEmpty()
                        ||mappedCO.isEmpty()
                        ||cognitiveLevel.isEmpty()
        ){
            throw new BadRequestException("the request must contain 'subjectCode' and 'mappedCO' and 'cognitiveLevel'");
        }
        return ResponseUtility.responseTemplateForSingleData(
                "successful",
                QuestionDtoUtil.questionToQuestionDTO_Paged(
                        questionService.findBySubjectCodeMappedCOCognitiveLevel(
                                subjectCode,
                                mappedCO,
                                cognitiveLevel,
                                pageNo,
                                size
                        )
                ),
                "All questions with selected subject code, mapped co and cognitiveLevel ",
                200
        );
    }

    @GetMapping("/questions/subjectName")
    @SneakyThrows
    public ResponseEntity<?> findBySubjectName(@RequestParam("subjectName") String subjectName){
        if (!subjectName.isEmpty()){
            throw new BadRequestException("the request must contain 'subjectName'");
        }
        return ResponseUtility.responseTemplateForMultipleData(
                "successful",
                QuestionDtoUtil.listOfQuestionToQuestionDto(
                        questionService.findBySubjectName(
                                subjectName
                        )
                ).toArray(),
                "questions with subject name "+subjectName,
                200
        );
    }

    @GetMapping("/questions/subjectName/pagged")
    @SneakyThrows
    public ResponseEntity<?> findBySubjectName(
            @RequestParam(value = "pageNo",defaultValue = "0")int pageNo,
            @RequestParam(value = "size",defaultValue = "100")int size,
            @RequestParam("subjectName") String subjectName
    ){
        if (subjectName.isEmpty()){
            throw new BadRequestException("the request must contain 'subjectName'");
        }
        return ResponseUtility.responseTemplateForSingleData(
                "successful",
                QuestionDtoUtil.questionToQuestionDTO_Paged(
                        questionService.findBySubjectName(
                                subjectName,
                                pageNo,
                                size
                        )
                ),
                "All questions with selected subject name :"+subjectName,
                200
        );
    }

    @GetMapping("/questions/subjectName/mappedCO")
    @SneakyThrows
    public ResponseEntity<?> findBySubjectNameMappedCO(
            @RequestParam("subjectName") String subjectName,
            @RequestParam("mappedCO") String mappedCO
    ){
        if (
                subjectName.isEmpty()
                        ||mappedCO.isEmpty()
        ){
            throw new BadRequestException("the request must contain 'subjectName' and 'mappedCO'");
        }
        return ResponseUtility.responseTemplateForMultipleData(
                "successful",
                questionService.findBySubjectNameMappedCO(
                        subjectName,
                        mappedCO
                ).toArray(),
                "questions with subject name and mapped Co"+subjectName+" "+mappedCO,
                200
        );
    }

    @GetMapping("/questions/subjectName/mappedCO/pagged")
    @SneakyThrows
    public ResponseEntity<?> findBySubjectNameMappedCO(
            @RequestParam(value = "pageNo", defaultValue = "0")int pageNo,
            @RequestParam(value = "size" , defaultValue = "100")int size,
            @RequestParam("subjectName") String subjectName,
            @RequestParam("mappedCO") String mappedCO
    ){
        if (
                subjectName.isEmpty()
                        ||mappedCO.isEmpty()
        ){
            throw new BadRequestException("the request must contain 'subjectName' and 'mappedCO'");
        }
        return ResponseUtility.responseTemplateForSingleData(
                "successful",
                QuestionDtoUtil.questionToQuestionDTO_Paged(
                        questionService.findBySubjectCodeMappedCO(
                                subjectName,
                                mappedCO,
                                pageNo,
                                size
                        )
                ),
                "all questions with selected subject name and mapped co ",
                200
        );
    }

    @GetMapping("/questions/subjectName/mappedCO/cognitiveLevel")
    @SneakyThrows
    public ResponseEntity<?> findBySubjectNameMappedCOCognitiveLevel(
            @RequestParam("subjectName") String subjectName,
            @RequestParam("mappedCO") String mappedCO,
            @RequestParam("cognitiveLevel")String cognitiveLevel
    ){
        if (
                subjectName.isEmpty()||
                        mappedCO.isEmpty()||
                        cognitiveLevel.isEmpty()
        ){
            throw new BadRequestException("the request must contain 'subjectName' and 'mappedCO' and 'cognitiveLevel'");
        }
        return ResponseUtility.responseTemplateForSingleData(
                "successful",
                QuestionDtoUtil.listOfQuestionToQuestionDto(
                        questionService.findBySubjectNameMappedCOCognitiveLevel(
                                subjectName,
                                mappedCO,
                                cognitiveLevel
                        )
                ),
                "All questions with selected subject name, mapped co and cognitiveLevel ",
                200
        );
    }

    @GetMapping("/questions/subjectName/mappedCO/cognitiveLevel/pagged")
    @SneakyThrows
    public ResponseEntity<?> findBySubjectNameMappedCOCognitiveLevel(
            @RequestParam(value = "pageNo", defaultValue = "0")int pageNo,
            @RequestParam(value = "size" , defaultValue = "100")int size,
            @RequestParam("subjectName") String subjectName,
            @RequestParam("mappedCO") String mappedCO,
            @RequestParam("cognitiveLevel")String cognitiveLevel
    ){
        if (
                subjectName.isEmpty()||
                        mappedCO.isEmpty()||
                        cognitiveLevel.isEmpty()
        ){
            throw new BadRequestException("the request must contain 'subjectName' and 'mappedCO' and 'cognitiveLevel'");
        }
        return ResponseUtility.responseTemplateForSingleData(
                "successful",
                QuestionDtoUtil.questionToQuestionDTO_Paged(
                        questionService.findBySubjectNameMappedCOCognitiveLevel(
                                subjectName,
                                mappedCO,
                                cognitiveLevel,
                                pageNo,
                                size
                        )
                ),
                "All questions with selected subject name, mapped co and cognitiveLevel ",
                200
        );
    }

    @GetMapping("/questions/user/email")
    @SneakyThrows
    public ResponseEntity<?> findByCreatedByUsingEmail(@RequestParam("email") String email){
        if (!email.contains("@")){
            throw new BadRequestException("the given email is not correct");
        }
        return ResponseUtility.responseTemplateForSingleData(
                "successful",
                QuestionDtoUtil.listOfQuestionToQuestionDto(
                        questionService.findByCreatedByUsingEmail(email)
                ),
                "all questions that are made my user with email :"+email,
                200
        );
    }

    @GetMapping("/questions/user/id")
    @SneakyThrows
    public ResponseEntity<?> findByCreatedByUsingId(@RequestParam("id")Long id){
        if (id.toString().isEmpty()){
            throw new BadRequestException("the id must contain some value");
        }
        return ResponseUtility.responseTemplateForSingleData(
                "successful",
                QuestionDtoUtil.listOfQuestionToQuestionDto(
                        questionService.findByCreatedByUsingId(id)
                ),
                "all questions that are made my user with id :"+id,
                200
        );
    }

    @GetMapping("/questionsPapers")
    public ResponseEntity<?> getAllQuestionPapers(){
        return ResponseUtility.responseTemplateForSingleData(
                "successful",
                QuestionPaperDtoUtil.listOfQuestionPaperToQuestionPaperDto(
                        questionPaperService.getAllQuestionPapers()
                ),
                "all question papers ",
                200
        );
    }

    @GetMapping("/questionsPapers/paged")
    public ResponseEntity<?> getAllQuestionsPaper(
            @RequestParam(value = "pageNo",defaultValue = "0")int pageNo,
            @RequestParam(value = "size" , defaultValue = "100")int size
    ){
        return ResponseUtility.responseTemplateForSingleData(
                "successful",
                QuestionPaperDtoUtil.questionPaperToQuestionPaperDtoPaged(
                        questionPaperService.getAllQuestionPapers(
                                pageNo, size
                        )
                ),
                "all questions papers",
                200
        );
    }

    @GetMapping("/questionsPapers/id")
    public ResponseEntity<?> getAllQuestionPapers(
           @RequestParam("id") Long id
    ){
        return ResponseUtility.responseTemplateForSingleData(
                "successful",
                QuestionPaperDtoUtil.questionPaperToQuestionPaperDto(
                        questionPaperService.findById(id)
                ),
                "question paper with id:"+id,
                200
        );
    }

    @GetMapping("/questionsPapers/examTitle")
    public ResponseEntity<?> findByExamTitle(
            @RequestParam String examTitle
    ){
        return ResponseUtility.responseTemplateForSingleData(
                "successful",
                QuestionPaperDtoUtil.questionPaperToQuestionPaperDto(
                        questionPaperService.findByExamTitle(examTitle)
                ),
                "question paper with exam title:"+examTitle,
                200
        );
    }

    @GetMapping("/questionsPapers/user/generatedBy/email")
    public ResponseEntity<?> findByGeneratedByUsingEmail(
            @RequestParam("email") String email
    ){
        return ResponseUtility.responseTemplateForSingleData(
                "successful",
                QuestionPaperDtoUtil.listOfQuestionPaperToQuestionPaperDto(
                        questionPaperService.findByGeneratedByUsingEmail(email)
                ),
                "all question papers my user with email:"+email,
                200
        );
    }

    @GetMapping("/questionsPapers/user/generatedBy/email/paged")
    public ResponseEntity<?> findByGeneratedByUsingEmail(
            @RequestParam(value = "pageNo" ,defaultValue =  "0") int pageNo,
            @RequestParam(value = "size", defaultValue = "100") int size,
            @RequestParam("email") String email
    ){
        return ResponseUtility.responseTemplateForSingleData(
                "successful",
                QuestionPaperDtoUtil.questionPaperToQuestionPaperDtoPaged(
                        questionPaperService.
                                findByGeneratedByUsingEmail(
                                        email,
                                        pageNo,
                                        size
                                )
                ),
                "all question papers my user with email:"+email,
                200
        );
    }

    @GetMapping("/questionsPapers/user/generatedBy/id")
    public ResponseEntity<?> findByGeneratedByUsingId(
            @RequestParam("id") Long id
    ){
        return ResponseUtility.responseTemplateForSingleData(
                "successful",
                QuestionPaperDtoUtil.listOfQuestionPaperToQuestionPaperDto(
                        questionPaperService.findByGeneratedByUsingId(id)
                ),
                "all question papers my user with id:"+id,
                200
        );
    }

    @GetMapping("/questionsPapers/user/generatedBy/id/paged")
    public ResponseEntity<?> findByGeneratedByUsingId(
            @RequestParam(value = "pageNo",defaultValue = "0")int pageNo,
            @RequestParam(value = "size",defaultValue = "100")int size,
            @RequestParam("id") Long id
    ){
        return ResponseUtility.responseTemplateForSingleData(
                "successful",
                QuestionPaperDtoUtil.questionPaperToQuestionPaperDtoPaged(
                        questionPaperService.findByGeneratedByUsingId(
                                id,
                                pageNo,
                                size
                        )

                ),
                "all question papers my user with id:"+id,
                200
        );
    }

    @GetMapping("/questionsPapers/user/approvedBy/email")
    @SneakyThrows
    public ResponseEntity<?> findByApprovedByUsingEmail(
            @RequestParam("email") String email
    ){
        if (!email.contains("@")){
            throw new BadRequestException("something is wrong with your email");
        }
        return ResponseUtility.responseTemplateForSingleData(
                "successful",
                QuestionPaperDtoUtil.listOfQuestionPaperToQuestionPaperDto(
                        questionPaperService.findByApprovedByUsingEmail(
                                email
                        )
                ),
                "all question papers approvedBy user with email:"+email,
                200
        );
    }

    @GetMapping("/questionsPapers/user/approvedBy/email/paged")
    public ResponseEntity<?> findByApprovedByUsingEmail(
            @RequestParam(value = "pageNo" ,defaultValue =  "0") int pageNo,
            @RequestParam(value = "size", defaultValue = "100") int size,
            @RequestParam("email") String email
    ){
        return ResponseUtility.responseTemplateForSingleData(
                "successful",
                QuestionPaperDtoUtil.questionPaperToQuestionPaperDtoPaged(
                        questionPaperService.findByApprovedByUsingEmail(
                                email,
                                pageNo,
                                size
                        )

                ),
                "all question papers approvedBy user with email:"+email,
                200
        );
    }

    @GetMapping("/questionsPapers/user/approvedBy/id")
    public ResponseEntity<?> findByApprovedByUsingId(
            @RequestParam("id") Long id
    ){
        return ResponseUtility.responseTemplateForSingleData(
                "successful",
                QuestionPaperDtoUtil.listOfQuestionPaperToQuestionPaperDto(
                        questionPaperService.findByApprovedByUsingId(id)
                ),
                "all question papers approvedBy user with id:"+id,
                200
        );
    }

    @GetMapping("/questionsPapers/user/approvedBy/id/paged")
    public ResponseEntity<?> findByApprovedByUsingId(
            @RequestParam(value = "pageNo",defaultValue = "0")int pageNo,
            @RequestParam(value = "size",defaultValue = "100")int size,
            @RequestParam("id") Long id
    ){
        return ResponseUtility.responseTemplateForSingleData(
                "successful",
                QuestionPaperDtoUtil.questionPaperToQuestionPaperDtoPaged(
                        questionPaperService.findByApprovedByUsingId(
                                id,
                                pageNo,
                                size
                        )

                ),
                "all question papers approvedBy user with id:"+id,
                200
        );
    }

    @GetMapping("/questionsPapers/approved")
    public ResponseEntity<?> findApproved(){
        return ResponseUtility.responseTemplateForSingleData(
                "successful",
                QuestionPaperDtoUtil.listOfQuestionPaperToQuestionPaperDto(
                        questionPaperService.findApproved()
                ),
                "all approved question papers ",
                200
        );
    }

    @GetMapping("/questionsPapers/approved/page")
    public ResponseEntity<?> findApproved(
            @RequestParam(value = "pageNo",defaultValue = "0")int pageNo,
            @RequestParam(value = "size" , defaultValue = "100")int size
    ){
        return ResponseUtility.responseTemplateForSingleData(
                "successful",
                QuestionPaperDtoUtil.questionPaperToQuestionPaperDtoPaged(
                        questionPaperService.findApproved(
                                pageNo,
                                size
                        )

                ),
                "all approved question papers",
                200
        );
    }

    @GetMapping("/questionsPapers/not-approved")
    public ResponseEntity<?> findNotApproved(){
        return ResponseUtility.responseTemplateForSingleData(
                "successful",
                QuestionPaperDtoUtil.listOfQuestionPaperToQuestionPaperDto(
                        questionPaperService.findNotApproved()
                ),
                "all not approved question papers ",
                200
        );
    }

    @GetMapping("/questionsPapers/not-approved/paged")
    public ResponseEntity<?> findNotApproved(
            @RequestParam(value = "pageNo",defaultValue = "0")int pageNo,
            @RequestParam(value = "size" , defaultValue = "100")int size
    ){
        return ResponseUtility.responseTemplateForSingleData(
                "successful",
                QuestionPaperDtoUtil.questionPaperToQuestionPaperDtoPaged(
                        questionPaperService.findNotApproved(
                                pageNo,
                                size
                        )
                ),
                "all not approved question papers",
                200
        );
    }

    @PatchMapping("/questionsPapers/approv/id")
    public ResponseEntity<?> approveQuestionPaperById(
            @RequestBody IdAndComment dto
            ){
        return ResponseUtility.responseTemplateForSingleData(
                "successful",
                QuestionPaperDtoUtil.questionPaperToQuestionPaperDto(
                        questionPaperService.approveQuestionPaperById(
                                dto.getId(),
                                dto.getComment()
                        )
                ),
                "the selected question has been approved with id:"+dto.getId(),
                200
        );
    }

    @PatchMapping("/questionsPapers/not-approv/id")
    public ResponseEntity<?> notApproveQuestionPaperById(
            @RequestBody IdAndComment dto
    ){
        return ResponseUtility.responseTemplateForSingleData(
                "successful",
                QuestionPaperDtoUtil.questionPaperToQuestionPaperDto(
                        questionPaperService.notApproveQuestionPaperById(
                                dto.getId(),
                                dto.getComment()
                        )
                ),
                "the selected question has been not approved with id:"+dto.getId(),
                200
        );
    }

    @PatchMapping("/questionsPapers/approv/examTitle")
    public ResponseEntity<?> approvedQuestionPaperByTile(
            @RequestBody ExamTitleAndComment dto
            ){
        return ResponseUtility.responseTemplateForSingleData(
                "successful",
                QuestionPaperDtoUtil.questionPaperToQuestionPaperDto(
                        questionPaperService.approvedQuestionPaperByTile(
                                dto.getExamTitle(),
                                dto.getComment()
                        )
                ),
                "the selected question has been approved with examTitle:"+dto.getExamTitle(),
                200
        );
    }

    @PatchMapping("/questionsPapers/not-approv/examTitle")
    public ResponseEntity<?> notApprovedQuestionPaperByTile(
            @RequestBody ExamTitleAndComment dto
    ){
        return ResponseUtility.responseTemplateForSingleData(
                "successful",
                QuestionPaperDtoUtil.questionPaperToQuestionPaperDto(
                        questionPaperService.notApprovedQuestionPaperByTile(
                                dto.getExamTitle(),
                                dto.getComment()
                        )
                ),
                "the selected question has been not approved with examTitle:"+dto.getExamTitle() ,
                200
        );
    }

    @PatchMapping("/update/user/email")
    @SneakyThrows
    public ResponseEntity<?>updateUserEmailById(
            @RequestBody Map<String,String> request
    ){
        if(!request.containsKey("email")){
            throw new BadRequestException("the request must contain email");
        }
        return ResponseUtility.responseTemplateForSingleData(
                "successful",
                UserDtoUtil.UserToUserDto(
                        userService.updateUserEmail(request.get("email"))
                ),
                "your email has been updated",
                200
        );
    }

    @PatchMapping("/update/user/password")
    @SneakyThrows
    public ResponseEntity<?> updateUserPasswordById(
            @RequestBody Map<String,String> request
    ){
        if (!request.containsKey("password")){
            throw new BadRequestException("the request must contain password");
        }
        return ResponseUtility.responseTemplateForSingleData(
                "successful",
                UserDtoUtil.UserToUserDto(
                        userService.updateUserPassword(request.get("password"))
                ),
                "your password has been updated",
                200
        );
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(){
        userService.logout();
        return ResponseUtility.responseTemplateForDeletedData(
                "successful",
                "you have been logout ",
                200
        );
    }

    @GetMapping("/download/questionsPapers/id")
    public ResponseEntity<InputStreamResource> downloadQuestionPaper(@RequestParam("id") Long id){
        ByteArrayInputStream file= questionPaperService.downloadQuestionPaper(id);

        HttpHeaders headers=new HttpHeaders();
        headers.add("Content-Disposition","inline; filename=Question-Paper.pdf");
        return ResponseEntity.ok().headers(headers).contentType(MediaType.APPLICATION_PDF).body(new InputStreamResource(file));
    }

    @GetMapping("/test")
    public ResponseEntity<?> test(){
        return ResponseEntity.ok(HttpStatus.OK);
    }

}
