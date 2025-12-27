package com.FinalYearProject.FinalYearProject.Controller;

import com.FinalYearProject.FinalYearProject.DTO.QuestionDto.DtoForSubjectCodeAndMappedCO;
import com.FinalYearProject.FinalYearProject.DTO.QuestionDto.DtoForSubjectCodeAndMappedCOAndCognitiveLevel;
import com.FinalYearProject.FinalYearProject.DTO.QuestionDto.DtoForSubjectNameAndMappedCO;
import com.FinalYearProject.FinalYearProject.DTO.QuestionDto.DtoForSubjectNameAndMappedCOAndCognitiveLevel;
import com.FinalYearProject.FinalYearProject.Domain.Question;
import com.FinalYearProject.FinalYearProject.Domain.User;
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
    @Autowired
    private UserService userService;
    @Autowired
    private QuestionService questionService;

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
    public PagedModel<Question>  getAllQuestionsPaged(
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
