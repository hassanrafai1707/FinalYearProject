package com.FinalYearProject.FinalYearProject.Controller;

import com.FinalYearProject.FinalYearProject.DTO.QuestionDto.*;
import com.FinalYearProject.FinalYearProject.Domain.User;
import com.FinalYearProject.FinalYearProject.Service.QuestionService;
import com.FinalYearProject.FinalYearProject.Service.UserService;
import com.FinalYearProject.FinalYearProject.Util.ResponseUtility;
import lombok.SneakyThrows;
import org.apache.coyote.BadRequestException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
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
    private final UserService userService;
    private final QuestionService questionService;

    public StudentRestController(UserService userService,QuestionService questionService){
        this.userService=userService;
        this.questionService=questionService;
    }

    private LocalDateTime getTimeNow(){
        return LocalDateTime.now();
    }

    @GetMapping("/questions")
    public ResponseEntity<?> getAllQuestion(){
        List<QuestionDTO> questionList=questionService.getAllQuestionWithDTO();
        return ResponseUtility.responseTemplateForMultipleData(
                "successful",
                questionList.toArray(),
                "All questions",
                200
        );
    }

    @GetMapping("/questions/paged")
    public ResponseEntity<?>  getAllQuestionsPaged(
            @RequestParam(value = "pageNo", defaultValue = "0") int pageNo,
            @RequestParam(value = "size",defaultValue = "100") int size
    ){
        return ResponseEntity.ok(
                Map.of(
                        "status","successful",
                        "data",questionService.getAllQuestionsDTOPagedImpl(pageNo, size),
                        "time",getTimeNow()
                )
        );
    }

    @GetMapping("/question/id/{id}")
    @SneakyThrows
    public ResponseEntity<?> getQuestionById (@RequestParam("id") Long id){
        if (id.toString().isEmpty()){
            throw new BadRequestException("the request must contain 'id'");
        }
        return ResponseUtility.responseTemplateForSingleData(
                "successful",
                questionService.getQuestionDtoById(id),
                "question with id "+id,
                200
        );
    }

    @GetMapping("/questions/subjectCode/{subjectCode}")
    @SneakyThrows
    public ResponseEntity<?> findBySubjectCode(@RequestParam("subjectCode") String subjectCode) {
        if (!subjectCode.isEmpty()){
            throw new BadRequestException("the request must contain 'subjectCode'");
        }
        return ResponseUtility.responseTemplateForMultipleData(
                "successful",
                questionService.findBySubjectCodeDto(subjectCode).toArray(),
                "questions with subject code "+subjectCode,
                200
        );
    }

    @GetMapping("/questions/subjectCode/pagged")
    @SneakyThrows
    public ResponseEntity<?> findBySubjectCode(
            @RequestParam(value = "pageNo" ,defaultValue = "0") int pageNo,
            @RequestParam(value = "size" , defaultValue = "100") int size,
            @RequestBody Map<String,String> request
    ){
        if (!request.containsKey("subjectCode")){
            throw new BadRequestException("the request must contain 'subjectCode'");
        }
        return ResponseEntity.ok(
                Map.of(
                        "status","successful",
                        "data",questionService.findBySubjectCodeDtoPaged(
                                request.get("subjectCode"),
                                pageNo,
                                size
                        ),
                        "time",getTimeNow()
                )
        );
    }

    @GetMapping("/questions/subjectCode/{subjectCode}/mappedCO/{mappedCO}")
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
                questionService.findBySubjectCodeMappedCODto(
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
            @RequestBody DtoForSubjectCodeAndMappedCO dto
    ){
        if (
                dto.getSubjectCode().isEmpty()
                ||dto.getMappedCO().isEmpty()
        ){
            throw new BadRequestException("the request must contain 'subjectCode' and 'mappedCO'");
        }
        return ResponseEntity.ok(
                Map.of(
                        "status","successful",
                        "data", questionService.findBySubjectCodeMappedCODtoPaged(
                              dto.getSubjectCode(),
                              dto.getMappedCO(),
                              pageNo,
                              size
                        ),
                        "time",getTimeNow()
                )
        );
    }

    @GetMapping("/questions/subjectCode/mappedCO/cognitiveLevel/pagged")
    @SneakyThrows
    private ResponseEntity<?> findByCognitiveLevel(
            @RequestParam(value = "pageNo",defaultValue = "0")int pageNo,
            @RequestParam(value = "size",defaultValue = "100")int size,
            @RequestBody DtoForSubjectCodeAndMappedCOAndCognitiveLevel dto
    ){
        if (
                dto.getSubjectCode().isEmpty()
                ||dto.getMappedCO().isEmpty()
                ||dto.getCognitiveLevel().isEmpty()
        ){
            throw new BadRequestException("the request must contain 'subjectCode' and 'mappedCO' and 'cognitiveLevel'");
        }
        return ResponseEntity
                .ok(
                        Map.of(
                                "status","successful",
                                "data",questionService.findBySubjectCodeMappedCOCognitiveLevelDtoPaged(
                                        dto.getSubjectCode(),
                                        dto.getMappedCO(),
                                        dto.getCognitiveLevel(),
                                        pageNo,
                                        size
                                ),
                                "time",getTimeNow()
                        )
                );
    }

    @GetMapping("/questions/subjectName/{subjectName}")
    @SneakyThrows
    public ResponseEntity<?> findBySubjectName(@RequestParam("subjectName") String subjectName){
        if (!subjectName.isEmpty()){
            throw new BadRequestException("the request must contain 'subjectName'");
        }
        return ResponseUtility.responseTemplateForMultipleData(
                "successful",
                questionService.findBySubjectNameDto(subjectName).toArray(),
                "questions with subject name "+subjectName,
                200
        );
    }

    @GetMapping("/questions/subjectName/pagged")
    @SneakyThrows
    public ResponseEntity<?> findBySubjectName(
            @RequestParam(value = "pageNo",defaultValue = "0")int pageNo,
            @RequestParam(value = "size",defaultValue = "100")int size,
            @RequestBody Map<String,String> request
    ){
        if (!request.containsKey("subjectName")){
            throw new BadRequestException("the request must contain 'subjectName'");
        }
        return ResponseEntity.ok(
                Map.of(
                        "status","successful",
                        "data",questionService.findBySubjectNameDtoPaged(
                                request.get("subjectName"),
                                pageNo,
                                size
                        ),
                        "time",getTimeNow()
                )
        );
    }

    @GetMapping("/questions/subjectName/{subjectName}/mappedCO/{mappedCO}")
    @SneakyThrows
    public ResponseEntity<?> findBySubjectNameMappedCO(@RequestBody DtoForSubjectNameAndMappedCO dto){
        if (
                dto.getSubjectName().isEmpty()
                ||dto.getMappedCO().isEmpty()
        ){
            throw new BadRequestException("the request must contain 'subjectName' and 'mappedCO'");
        }
        return ResponseUtility.responseTemplateForMultipleData(
                "successful",
                questionService.findBySubjectNameMappedCODto(
                        dto.getSubjectName(),
                        dto.getMappedCO()
                ).toArray(),
                "questions with subject name and mapped Co"+dto.getSubjectName()+" "+dto.getMappedCO(),
                200
        );
    }

    @GetMapping("/questions/subjectName/mappedCO/pagged")
    @SneakyThrows
    public ResponseEntity<?> findBySubjectNameMappedCO(
            @RequestParam(value = "pageNo", defaultValue = "0")int pageNo,
            @RequestParam(value = "size" , defaultValue = "100")int size,
            @RequestBody DtoForSubjectNameAndMappedCO dto
    ){
        if (
                dto.getSubjectName().isEmpty()
                ||dto.getMappedCO().isEmpty()
        ){
            throw new BadRequestException("the request must contain 'subjectName' and 'mappedCO'");
        }
        return ResponseEntity.ok(
                Map.of(
                        "status","successful",
                        "data",questionService.findBySubjectNameMappedCODtoPaged(
                                dto.getSubjectName(),
                                dto.getMappedCO(),
                                pageNo,
                                size
                        ),
                        "time",getTimeNow()
                )
        );
    }

    @GetMapping("/questions/subjectName/mappedCO/cognitiveLevel/pagged")
    @SneakyThrows
    public ResponseEntity<?> findBySubjectNameMappedCOCognitiveLevel(
            @RequestParam(value = "pageNo", defaultValue = "0")int pageNo,
            @RequestParam(value = "size" , defaultValue = "100")int size,
            @RequestBody DtoForSubjectNameAndMappedCOAndCognitiveLevel dto
    ){
        if (
                dto.getSubjectName().isEmpty()
                ||dto.getMappedCO().isEmpty()
                ||dto.getCognitiveLevel().isEmpty()
        ){
            throw new BadRequestException("the request must contain 'subjectName' and 'mappedCO' and 'cognitiveLevel'");
        }
        return ResponseEntity
                .ok(
                        Map.of(
                                "status","successful",
                                "data", questionService.findBySubjectNameMappedCOCognitiveLevelDtoPaged(
                                        dto.getSubjectName(),
                                        dto.getMappedCO(),
                                        dto.getCognitiveLevel(),
                                        pageNo,
                                        size
                                ),
                                "time",getTimeNow()
                        )
                );
    }

    @PatchMapping("/update/user/email")
    public ResponseEntity<?>updateUserEmailById(@RequestBody Map<String,String> request
    ){
        String email= request.get("email");
        User upDatedUser= userService.updateUserEmail(email);
        return ResponseEntity.ok(
                Map.of(
                        "status","successful",
                        "data",upDatedUser,
                        "time",getTimeNow()
                )
        );
    }

    @PatchMapping("/update/user/password")
    public ResponseEntity<?> updateUserPasswordById(@RequestBody Map<String,String> request){
        String password= request.get("password");
        User updatedUser = userService.updateUserPassword(password);
        return ResponseEntity.ok(
                Map.of(
                        "status","successful",
                        "data",updatedUser,
                        "time",getTimeNow()
                )
        );
    }

    @GetMapping("/test")
    public ResponseEntity<?> test(){
        return ResponseEntity.ok(HttpStatus.OK);
    }
}
