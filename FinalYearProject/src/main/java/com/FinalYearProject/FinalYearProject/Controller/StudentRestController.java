package com.FinalYearProject.FinalYearProject.Controller;

import com.FinalYearProject.FinalYearProject.DTO.QuestionDto.*;
import com.FinalYearProject.FinalYearProject.Service.QuestionService;
import com.FinalYearProject.FinalYearProject.Service.UserService;
import com.FinalYearProject.FinalYearProject.Util.QuestionDtoUtil;
import com.FinalYearProject.FinalYearProject.Util.ResponseUtility;
import com.FinalYearProject.FinalYearProject.Util.UserDtoUtil;
import lombok.SneakyThrows;
import org.apache.coyote.BadRequestException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Student REST Controller for Question Retrieval and Self-Service Operations
 * PURPOSE: Provides question bank access and self-service account management for students. Requires ROLE_STUDENT or ROLE_ADMIN authority. All endpoints are version-prefixed with student path.
 * QUESTION RETRIEVAL OPERATIONS:
 * 1. GET ALL QUESTIONS: getAllQuestion returns all questions. getAllQuestionPaged returns paginated questions with default page size 100.
 * 2. GET BY ID: getQuestionById retrieves single question by database ID.
 * 3. FILTER BY SUBJECT CODE: findBySubjectCode returns questions for specific subject code. Supports pagination via findBySubjectCodePagged.
 * 4. FILTER BY SUBJECT CODE & MAPPED CO: findBySubjectCode-MappedCO returns questions filtered by subject code and course outcome. Supports pagination via findBySubjectCode-MappedCOPaged.
 * 5. FILTER BY SUBJECT CODE, MAPPED CO & COGNITIVE LEVEL: findBySubjectCode-MappedCO-CognitiveLevel returns questions filtered by subject code, course outcome, and cognitive level (Bloom's taxonomy).
 * 6. FILTER BY SUBJECT NAME: findBySubjectName returns questions for specific subject name. Supports pagination via findBySubjectNamePaged.
 * 7. FILTER BY SUBJECT NAME & MAPPED CO: findBySubjectName-MappedCO returns questions filtered by subject name and course outcome. Supports pagination via findBySubjectName-MappedCOPaged.
 * 8. FILTER BY SUBJECT NAME, MAPPED CO & COGNITIVE LEVEL: findBySubjectName-MappedCO-CognitiveLevel returns questions filtered by subject name, course outcome, and cognitive level.
 * STUDENT SELF-SERVICE OPERATIONS:
 * 1. ACCOUNT UPDATE: updateUserEmail allows students to change their email. updateUserPassword allows password changes.
 * 2. TEST ENDPOINT: test provides simple connectivity verification.
 * RESPONSE FORMAT: Consistent JSON structure: {"status": "Successful", "dataKey": data}. Paginated endpoints return PagedModel with pagination metadata.
 * SECURITY CONTEXT: Requires student or admin role. JWT token validation via SecurityFilterChain. Students can only modify their own account (handled in UserService).
 * PERFORMANCE FEATURES: Pagination support for all list endpoints (default 100 items per page). Efficient database queries with proper indexing. Consider caching for frequently accessed question banks.
 * EDUCATIONAL CONTEXT: Questions organized by Bloom's taxonomy cognitive levels (Remember, Understand, Apply, Analyze, Evaluate, Create). Mapped to course outcomes for curriculum alignment. Supports outcome-based education assessment.
 */
@RequestMapping("${app.version}/student")
@RestController
public class StudentRestController {
    private final UserService userService;
    private final QuestionService questionService;

    public StudentRestController(UserService userService,QuestionService questionService){
        this.userService=userService;
        this.questionService=questionService;
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

    @PostMapping("/logout")
    public ResponseEntity<?> logout(){
        userService.logout();
        return ResponseUtility.responseTemplateForSingleData(
                "successful",
                new Object(),
                "you have been logout ",
                200
        );
    }

    @GetMapping("/test")
    public ResponseEntity<?> test(){
        return ResponseEntity.ok(HttpStatus.OK);
    }
}
