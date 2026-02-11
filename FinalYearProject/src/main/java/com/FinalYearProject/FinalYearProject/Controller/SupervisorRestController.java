package com.FinalYearProject.FinalYearProject.Controller;

import com.FinalYearProject.FinalYearProject.Domain.QuestionPaper;
import com.FinalYearProject.FinalYearProject.Domain.User;
import com.FinalYearProject.FinalYearProject.Service.QuestionPaperService;
import com.FinalYearProject.FinalYearProject.Service.QuestionService;
import com.FinalYearProject.FinalYearProject.Service.UserService;
import com.FinalYearProject.FinalYearProject.Util.QuestionDtoUtil;
import com.FinalYearProject.FinalYearProject.Util.QuestionPaperDtoUtil;
import com.FinalYearProject.FinalYearProject.Util.QuestionPaperUtil;
import com.FinalYearProject.FinalYearProject.Util.ResponseUtility;
import lombok.SneakyThrows;
import org.apache.coyote.BadRequestException;
import org.springframework.data.web.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
                        questionService.getAllQuestionsPaged(pageNo, size),
                        pageNo,
                        size
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
                        ),
                        pageNo,
                        size
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
                        ),
                        pageNo,
                        size
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
                        ),
                        pageNo,
                        size
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
                        ),
                        pageNo,
                        size
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
                        ),
                        pageNo,
                        size
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
                        ),
                        pageNo,
                        size
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
                        ),
                        pageNo,
                        size
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

    @GetMapping("/findByGeneratedByUsingEmail")
    public ResponseEntity<?> findByGeneratedByUsingEmail(
            @RequestBody Map<String,String> request
    ){
        return ResponseEntity
                .ok(
                        Map.of(
                                "status","Successful",
                                "questionPaper",questionPaperService.findByGeneratedByUsingEmail(request.get("email"))
                        )
                );
    }

    @GetMapping("/findByGeneratedByUsingEmailPaged")
    public PagedModel<QuestionPaper> findByGeneratedByUsingEmail(
            @RequestParam(value = "pageNo" ,defaultValue =  "0") int pageNo,
            @RequestParam(value = "size", defaultValue = "100") int size,
            @RequestBody Map<String,String> request
    ){
        return new PagedModel<>(questionPaperService.
                        findByGeneratedByUsingEmail(
                                request.get("email"),
                                pageNo,
                                size
                        )
        );
    }

    @GetMapping("/findByGeneratedByUsingId")
    public ResponseEntity<?> findByGeneratedByUsingId(
            @RequestBody Map<String,Long> request
    ){
        return ResponseEntity.ok(
                Map.of(
                        "status","Successful",
                        "questionPaper",questionPaperService.findByGeneratedByUsingId(request.get("id"))
                )
        );
    }

    @GetMapping("/findByGeneratedByUsingIdPaged")
    public PagedModel<QuestionPaper> findByGeneratedByUsingId(
            @RequestParam(value = "pageNo",defaultValue = "0")int pageNo,
            @RequestParam(value = "size",defaultValue = "100")int size,
            @RequestBody Map<String,Long> request
    ){
        return new PagedModel<>(questionPaperService.findByGeneratedByUsingId(request.get("id"),pageNo,size));
    }

    @GetMapping("/findByApprovedByUsingEmail")
    public ResponseEntity<?> findByApprovedByUsingEmail(
            @RequestBody Map<String,String> request
    ){
        return ResponseEntity.ok(
                Map.of(
                        "status","Successful",
                        "questionPaper",questionPaperService.findByApprovedByUsingEmail(request.get("email"))
                )
        );
    }

    @GetMapping("/findByApprovedByUsingEmailPaged")
    public PagedModel<QuestionPaper> findByApprovedByUsingEmail(
            @RequestParam(value = "pageNo" ,defaultValue =  "0") int pageNo,
            @RequestParam(value = "size", defaultValue = "100") int size,
            @RequestBody Map<String,String> request
    ){
        return new PagedModel<>(
                questionPaperService.findByApprovedByUsingEmail(
                        request.get("email"),
                        pageNo,
                        size
                )
        );
    }

    @GetMapping("/findByApprovedByUsingId")
    public ResponseEntity<?> findByApprovedByUsingId(
            @RequestBody Map<String,Long> request
    ){
        return ResponseEntity.ok(
                Map.of(
                        "status","Successful",
                        "questionPaper",questionPaperService.findByApprovedByUsingId(request.get("id"))
                )
        );
    }

    @GetMapping("/findByApprovedByUsingIdPaged")
    public PagedModel<QuestionPaper> findByApprovedByUsingId(
            @RequestParam(value = "pageNo",defaultValue = "0")int pageNo,
            @RequestParam(value = "size",defaultValue = "100")int size,
            @RequestBody Map<String,Long> request
    ){
        return new PagedModel<>(
                questionPaperService.findByApprovedByUsingId(
                        request.get("id"),
                        pageNo,
                        size
                )
        );
    }

    @GetMapping("/findApproved")
    public ResponseEntity<?> findApproved(){
        return ResponseEntity.ok(
                Map.of(
                        "status","Successful",
                        "questionPaper",questionPaperService.findApproved()
                )
        );
    }

    @GetMapping("/findApprovedPaged")
    public PagedModel<QuestionPaper> findApproved(
            @RequestParam(value = "pageNo",defaultValue = "0")int pageNo,
            @RequestParam(value = "size" , defaultValue = "100")int size
    ){
        return new PagedModel<>(questionPaperService.findApproved(pageNo, size));
    }

    @GetMapping("/findNotApproved")
    public ResponseEntity<?> findNotApproved(){
        return ResponseEntity.ok(
                Map.of(
                        "status","Successful",
                        "questionPaper",questionPaperService.findNotApproved()
                )
        );
    }

    @GetMapping("/findNotApprovedPaged")
    public PagedModel<QuestionPaper> findNotApproved(
            @RequestParam(value = "pageNo",defaultValue = "0")int pageNo,
            @RequestParam(value = "size" , defaultValue = "100")int size
    ){
        return new PagedModel<>(questionPaperService.findNotApproved(pageNo, size));
    }

    @PatchMapping("/approveQuestionPaperById")
    public ResponseEntity<?> approveQuestionPaperById(
            @RequestBody Map<String,Long> request
    ){
        return ResponseEntity.ok(
                Map.of(
                        "status","Successful",
                        "approved Question paper",questionPaperService.
                                approveQuestionPaperById(
                                        request.get("id")
                                )
                )
        );
    }

    @PatchMapping("/notApproveQuestionPaperById")
    public ResponseEntity<?> notApproveQuestionPaperById(
            @RequestBody Map<String,Long> request
    ){
        return ResponseEntity.ok(
                Map.of(
                        "status","Successful",
                        "not approved Question paper",questionPaperService
                                .notApproveQuestionPaperById(
                                request.get("id")
                        )
                )
        );
    }

    @PatchMapping("/approvedQuestionPaperByTile")
    public ResponseEntity<?> approvedQuestionPaperByTile(
            @RequestBody Map<String,String> request
    ){
        return ResponseEntity.ok(
                Map.of(
                        "status","Successful",
                        "approved Question paper",questionPaperService
                                .approvedQuestionPaperByTile(
                                        request.get("examTitle")
                                )
                )
        );
    }

    @PatchMapping("/notApprovedQuestionPaperByTile")
    public ResponseEntity<?> notApprovedQuestionPaperByTile(
            @RequestBody Map<String,String> request
    ){
        return ResponseEntity.ok(
                Map.of(
                        "status","Successful",
                        "not approved Question paper",questionPaperService
                                .notApprovedQuestionPaperByTile(request.get("examTitle"))
                )
        );
    }

    @PatchMapping("/updateUserEmail")
    public ResponseEntity<?>updateUserEmailById(@RequestBody Map<String,String> request
    ){
        String email= request.get("email");
        User upDatedUser= userService.updateUserEmail(email);
        return ResponseEntity.ok(
                Map.of(
                        "states","successful",
                        "updatedUser",upDatedUser
                )
        );
    }

    @PatchMapping("/updateUserPassword")
    public ResponseEntity<?> updateUserPasswordById(@RequestBody Map<String,String> request){
        String password= request.get("password");
        User updatedUser = userService.updateUserPassword(password);
        return ResponseEntity.ok(
                Map.of(
                        "states","successful",
                        "updatedUser",updatedUser
                )
        );
    }

    @GetMapping("/test")
    public ResponseEntity<?> test(){
        return ResponseEntity.ok(HttpStatus.OK);
    }

}
