package com.FinalYearProject.FinalYearProject.Controller;

import com.FinalYearProject.FinalYearProject.DTO.QuestionDto.DtoForSubjectCodeAndMappedCO;
import com.FinalYearProject.FinalYearProject.DTO.QuestionDto.DtoForSubjectCodeAndMappedCOAndCognitiveLevel;
import com.FinalYearProject.FinalYearProject.DTO.QuestionDto.DtoForSubjectNameAndMappedCO;
import com.FinalYearProject.FinalYearProject.DTO.QuestionDto.DtoForSubjectNameAndMappedCOAndCognitiveLevel;
import com.FinalYearProject.FinalYearProject.Domain.Question;
import com.FinalYearProject.FinalYearProject.Domain.QuestionPaper;
import com.FinalYearProject.FinalYearProject.Domain.User;
import com.FinalYearProject.FinalYearProject.Service.QuestionPaperService;
import com.FinalYearProject.FinalYearProject.Service.QuestionService;
import com.FinalYearProject.FinalYearProject.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.web.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
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
    @Autowired
    private UserService userService;
    @Autowired
    private QuestionService questionService;
    @Autowired
    private QuestionPaperService questionPaperService;

    @GetMapping("/getAllQuestion")
    public ResponseEntity<?> getAllQuestion(){
        List<Question> questionList=questionService.getAllQuestion();
        return ResponseEntity
                .ok(
                        Map.of(
                                "status","Successful",
                                "all Question", questionList
                        )
                );
    }

    @GetMapping("/getAllQuestionPaged")
    public PagedModel<Question> getAllQuestionsPaged(
            @RequestParam(value = "pageNo", defaultValue = "0") int pageNo,
            @RequestParam(value = "size",defaultValue = "100") int size
    ){
        return new PagedModel<>(questionService.getAllQuestionsPaged(pageNo,size));
    }

    @GetMapping("/getQuestionById")
    public ResponseEntity<?> getQuestionById (@RequestBody Map<String,Long> request){
        return ResponseEntity
                .ok(
                        Map.of(
                                "status","Successful",
                                "question with giver id",questionService.getQuestionById(request.get("id"))
                        )
                );
    }

    @GetMapping("/findBySubjectCode")
    public ResponseEntity<?> findBySubjectCode(@RequestBody Map<String,String> request){
        String subjectCode =request.get("subjectCode");
        List<Question> questions=questionService.findBySubjectCode(subjectCode);
        return ResponseEntity
                .ok(
                        Map.of(
                                "status","Successful",
                                "list of all questions with CO" ,questions
                        )
                );
    }

    @GetMapping("/findBySubjectCodePagged")
    public PagedModel<Question> findBySubjectCode(
            @RequestParam(value = "pageNo" ,defaultValue = "0") int page,
            @RequestParam(value = "size" , defaultValue = "100") int size,
            @RequestBody Map<String,String> request
    ){
        String subjectCode =request.get("subjectCode");
        return new PagedModel<>(questionService.findBySubjectCode(subjectCode,page,size));
    }

    @GetMapping("/findBySubjectCode-MappedCO")
    public ResponseEntity<?> findBySubjectCodeMappedCO(@RequestBody DtoForSubjectCodeAndMappedCO dto){
        List<Question> question=questionService.findBySubjectCodeMappedCO(dto.getSubjectCode(), dto.getMappedCO());
        return ResponseEntity
                .ok(
                        Map.of(
                                "status","Successful",
                                "list of all questions with subject name and mapped CO" ,question
                        )
                );
    }

    @GetMapping("/findBySubjectCode-MappedCOPaged")
    public PagedModel<Question> findBySubjectCodeMappedCO(
            @RequestParam(value = "pageNo",defaultValue = "0")int pageNo,
            @RequestParam(value = "size",defaultValue = "100")int size,
            @RequestBody DtoForSubjectCodeAndMappedCO dto
    ){
        return new PagedModel<>(
                questionService.findBySubjectCodeMappedCO(
                        dto.getSubjectCode(),
                        dto.getMappedCO(),
                        pageNo,
                        size
                )
        );
    }

    @GetMapping("/findBySubjectCode-MappedCO-CognitiveLevel")
    private ResponseEntity<?> findByCognitiveLevel(@RequestBody DtoForSubjectCodeAndMappedCOAndCognitiveLevel dto){
        List<Question> questions=questionService.findBySubjectCodeMappedCOCognitiveLevel(
                dto.getSubjectCode(),
                dto.getMappedCO(),
                dto.getCognitiveLevel()
        );
        return ResponseEntity
                .ok(
                        Map.of(
                                "status","Successful",
                                "list of all questions with subject code and mapped CO and cognitive level",questions
                        )
                );
    }

    @GetMapping("/findBySubjectName")
    public ResponseEntity<?> findBySubjectName(@RequestBody Map<String,String> request){
        String subjectName = request.get("subjectName");
        List<Question> questions=questionService.findBySubjectName(subjectName);
        return ResponseEntity
                .ok(
                        Map.of(
                                "status","Successful",
                                "list of all questions with subject name" ,questions
                        )
                );
    }

    @GetMapping("/findBySubjectNamePaged")
    public PagedModel<Question> findBySubjectName(
            @RequestParam(value = "pageNo",defaultValue = "0")int pageNo,
            @RequestParam(value = "size",defaultValue = "100")int size,
            @RequestBody Map<String,String> request
    ){
        String subjectName=request.get("subjectName");
        return new PagedModel<>(
                questionService.findBySubjectName(
                        subjectName,
                        pageNo,
                        size
                )
        );
    }

    @GetMapping("/findBySubjectName-MappedCO")
    public ResponseEntity<?> findBySubjectNameMappedCO(@RequestBody DtoForSubjectNameAndMappedCO dto){
        return ResponseEntity
                .ok(
                        Map.of(
                                "status","Successful",
                                "list of all questions with subject name and mapped CO",
                                questionService.findBySubjectNameMappedCO(
                                        dto.getSubjectName(),
                                        dto.getMappedCO()
                                )
                        )
                );
    }

    @GetMapping("/findBySubjectName-MappedCOPaged")
    public PagedModel<Question> findBySubjectNameMappedCO(
            @RequestParam(value = "pageNo", defaultValue = "0")int pageNo,
            @RequestParam(value = "size" , defaultValue = "100")int size,
            @RequestBody DtoForSubjectNameAndMappedCO dto
    ){
        return new PagedModel<>(
                questionService.findBySubjectNameMappedCO(
                        dto.getSubjectName(),
                        dto.getMappedCO(),
                        pageNo,
                        size
                )
        );
    }

    @GetMapping("/findBySubjectName-MappedCO-CognitiveLevel")
    public ResponseEntity<?> findBySubjectNameMappedCOCognitiveLevel(@RequestBody DtoForSubjectNameAndMappedCOAndCognitiveLevel dto){
        return ResponseEntity
                .ok(
                        Map.of(
                                "status" ,"Successful",
                                "list of all questions with subject name and mapped CO and cognitive level",
                                questionService.findBySubjectNameMappedCOCognitiveLevel(
                                        dto.getSubjectName(),
                                        dto.getMappedCO(),
                                        dto.getCognitiveLevel()
                                )
                        )
                );
    }

    @GetMapping("/findByCreatedByUsingEmail")
    public ResponseEntity<?> findByCreatedByUsingEmail(@RequestBody Map<String,String> request){
        String email= request.get("email");
        List<Question> questions = questionService.findByCreatedByUsingEmail(email);
        return ResponseEntity
                .ok(
                        Map.of(
                                "status","Successful",
                                "list of all questions with CO" ,questions
                        )
                );
    }

    @GetMapping("/findByCreatedByUsingId")
    public ResponseEntity<?> findByCreatedByUsingId(@RequestBody Map<String,Long> request){
        Long Id=request.get("id");
        List<Question> questions =questionService.findByCreatedByUsingId(Id);
        return ResponseEntity
                .ok(
                        Map.of(
                                "status","Successful",
                                "list of all questions with CO" ,questions
                        )
                );
    }

    @GetMapping("/getAllQuestionsPaper")
    public ResponseEntity<?> getAllQuestionPapers(){
        return ResponseEntity.
                ok(
                        Map.of(
                                "status","Successful",
                                "allQuestionsPaper",questionPaperService.getAllQuestionPapers()
                        )
                );
    }

    @GetMapping("/getAllQuestionsPaperPaged")
    public PagedModel<QuestionPaper> getAllQuestionsPaper(
            @RequestParam(value = "pageNo",defaultValue = "0")int pageNo,
            @RequestParam(value = "size" , defaultValue = "100")int size
    ){
        return new PagedModel<>(questionPaperService.getAllQuestionPapers(pageNo, size));
    }

    @GetMapping("findQuestionPaperById")
    public ResponseEntity<?> getAllQuestionPapers(
            @RequestBody Map<String,Long> request
    ){
        return ResponseEntity.ok(
                Map.of(
                        "status","Successful",
                        "questionPaper",questionPaperService.findById(request.get("id"))
                )
        );
    }

    @GetMapping("/findByExamTitle")
    public ResponseEntity<?> findByExamTitle(
            @RequestBody Map<String,String> request
    ){
        return ResponseEntity.ok(
                Map.of(
                        "status","Successful",
                        "questionPaper",questionPaperService.findByExamTitle(request.get("examTitle"))
                )
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
