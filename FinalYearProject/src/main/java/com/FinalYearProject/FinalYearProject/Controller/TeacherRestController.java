package com.FinalYearProject.FinalYearProject.Controller;

import com.FinalYearProject.FinalYearProject.DTO.QuestionDto.*;
import com.FinalYearProject.FinalYearProject.Domain.Question;
import com.FinalYearProject.FinalYearProject.Domain.QuestionPaper;
import com.FinalYearProject.FinalYearProject.Service.QuestionPaperService;
import com.FinalYearProject.FinalYearProject.Service.QuestionService;
import com.FinalYearProject.FinalYearProject.Service.UserService;
import com.FinalYearProject.FinalYearProject.Util.*;
import lombok.SneakyThrows;
import org.apache.coyote.BadRequestException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Teacher REST Controller for Question Management and Paper Generation
 * PURPOSE: Provides comprehensive question creation, management, and exam paper generation for teachers. Requires ROLE_TEACHER or ROLE_ADMIN authority. Combines CRUD operations for questions with intelligent paper generation algorithms.
 * QUESTION MANAGEMENT OPERATIONS:
 * 1. QUESTION CRUD: addQuestion creates new questions. deleteQuestionById deletes by ID. deleteQuestionByQuestionBody deletes by content text. getYourQuestion retrieves teacher's own questions (with pagination support).
 * 2. QUESTION RETRIEVAL: getAllQuestion returns all questions in bank. getQuestionById gets single question. findBySubjectCode/SubjectName with filtering variants (MappedCO, CognitiveLevel) similar to student endpoints.
 * 3. INTELLIGENT PAPER GENERATION: generateBySubjectCodeQuestionPaper creates balanced papers based on subject code, course outcomes, cognitive levels (A/R/U), and mark distribution. generateBySubjectNameAndQuestionPaper does same using subject name.
 * 4. PAPER APPROVAL WORKFLOW: approveGeneratedQuestionPaper submits generated papers for supervisor approval (saves to QuestionPaper entity).
 * PAPER GENERATION ALGORITHM FEATURES: Balances questions across cognitive levels (A=Apply, R=Remember, U=Understand). Distributes marks between 2-mark and 4-mark questions. Maps to specified course outcomes (MappedCOs). Ensures curriculum coverage and difficulty distribution.
 * SECURITY CONTEXT: Teachers can only delete their own questions. Paper generation uses full question bank but tracks creator. Submitted papers await supervisor approval before becoming official.
 * SELF-SERVICE OPERATIONS: updateUserEmail and updateUserPassword for account management. test endpoint for connectivity verification.
 * RESPONSE FORMAT: Consistent JSON structure: {"status": "Successful", "dataKey": data}. Generated papers return list of Question objects with balanced attributes.
 * EDUCATIONAL ALIGNMENT: Supports outcome-based education with course outcome mapping. Implements Bloom's taxonomy cognitive levels. Enforces mark distribution guidelines. Maintains question quality through creator tracking.
 */
@RequestMapping("${app.version}/teacher")
@RestController
public class TeacherRestController {
    private final UserService userService;
    private final QuestionService questionService;
    private final QuestionPaperService questionPaperService;

    public TeacherRestController(UserService userService,QuestionService questionService,QuestionPaperService questionPaperService){
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

    @PostMapping("/question")
    public ResponseEntity<?> addQuestion(@RequestBody Question question){
        return ResponseUtility.responseTemplateForSingleData(
                "successful",
                QuestionDtoUtil.QuestionToQuestionDto(
                        questionService.addQuestion(question)
                ),
                "Question crated successfully",
                201
        );
    }

    @GetMapping("/generate/question-paper/subjectCode")
    public ResponseEntity<?> generateBySubjectCodeQuestionPaper(@RequestBody DtoForSubjectCodeAndMappedCOs_ARU_And_2_4_Marks dto){
        return ResponseUtility.responseTemplateForSingleData(
                "successful",
                QuestionDtoUtil.listOfQuestionToQuestionDto(
                        questionService.generateBySubjectCodeQuestion(
                        dto.getSubjectCode(),
                        dto.getMappedCOs(),
                        dto.getNumberOfCognitiveLevel_A(),
                        dto.getNumberOfCognitiveLevel_R(),
                        dto.getNumberOfCognitiveLevel_U(),
                        dto.getMaxNumberOf2Marks(),
                        dto.getMaxNumberOf4Marks()
                        )
                ),
                "question paper has been gendered successfully copy all ids and past in ",
                200
        );
    }

    @GetMapping("/generate/question-paper/subjectName")
    public ResponseEntity<?> generateBySubjectAndQuestionPaper(@RequestBody DtoForSubjectNameAndMappedCOs_ARU_And_2_4_Marks dto){
        return ResponseUtility.responseTemplateForSingleData(
                "successful",
                QuestionDtoUtil.listOfQuestionToQuestionDto(
                        questionService.generateBySubjectNameQuestion(
                                dto.getSubjectName(),
                                dto.getMappedCOs(),
                                dto.getNumberOfCognitiveLevel_A(),
                                dto.getNumberOfCognitiveLevel_R(),
                                dto.getNumberOfCognitiveLevel_U(),
                                dto.getMaxNumberOf2Marks(),
                                dto.getMaxNumberOf4Marks()
                        )
                ),
                "question paper has been gendered successfully copy all ids and past in ",
                200
        );
    }

    @GetMapping("/my/questions")
    public ResponseEntity<?> getYourQuestion(){
        return ResponseUtility.responseTemplateForSingleData(
                "successful",
                QuestionDtoUtil.listOfQuestionToQuestionDto(
                        questionService.getAllQuestionsByCurrentUser()
                ),
                "My questions ",
                200
        );
    }

    @GetMapping("/my/questions/pagged")
    public ResponseEntity<?> getYourQuestion(
            @RequestParam(value = "pageNo",defaultValue = "0")int pageNo,
            @RequestParam(value = "size",defaultValue = "100")int size
    ){
        return ResponseUtility.responseTemplateForSingleData(
                "successful",
                QuestionDtoUtil.questionToQuestionDTO_Paged(
                        questionService.getAllQuestionsByCurrentUser(
                                pageNo,
                                size
                        ),
                        pageNo,
                        size
                ),
                "My questions ",
                200
        );
    }

    @SneakyThrows
    @PostMapping("/To-approve/questionPaper")
    public ResponseEntity<?> approveGeneratedQuestionPaper(@RequestBody QuestionPaper questions){
        return ResponseUtility.responseTemplateForSingleData(
                "successful",
               QuestionPaperDtoUtil.questionPaperToQuestionPaperDto(
                       questionPaperService.addQuestionPaper(questions)
               ),
                "Question Paper added successfully",
                200
        );
    }

    @DeleteMapping("/question/id")
    public ResponseEntity<?> deleteQuestionById(@RequestBody Map<String,Long> request){
        questionService.deleteQuestionById(request.get("id"));
        return ResponseUtility.responseTemplateForSingleData(
                "successful",
                new Question(),
                "selected question has been deleted",
                401
        );
    }

    @PatchMapping("/update/user/email")
    @SneakyThrows
    public ResponseEntity<?>updateUserEmailById(@RequestBody Map<String,String> request
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
    public ResponseEntity<?> updateUserPasswordById(@RequestBody Map<String,String> request){
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

    @GetMapping("/test")
    public ResponseEntity<?> test(){
        return ResponseEntity.ok(HttpStatus.OK);
    }
}
