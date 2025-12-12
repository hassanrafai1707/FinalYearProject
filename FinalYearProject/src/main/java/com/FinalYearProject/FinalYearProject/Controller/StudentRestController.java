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
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RequestMapping("${app.version}/student")
@RestController
public class StudentRestController {
    @Autowired
    private UserService userService;
    @Autowired
    private QuestionService questionService;

    @GetMapping("/getAllQuestion")
    public ResponseEntity<?> getAllQuestion(){
        return ResponseEntity
                .ok(
                        Map.of(
                                "status","Successful",
                                "all Question", questionService.getAllQuestion()
                        )
                );
    }

    @GetMapping("/getQuestionById")
    public ResponseEntity<?> getQuestionById(@RequestBody Map<String, Long> request){
        return ResponseEntity
                .ok(
                        Map.of(
                                "status","Successful",
                                "Question",questionService.getQuestionById(request.get("id"))
                        )
                );
    }

    @GetMapping("/findBySubjectName")
    public ResponseEntity<?> findBySubjectName(@RequestBody Map<String,String> request){
        String subjectName = request.get("subjectName");
        return ResponseEntity
                .ok(
                        Map.of(
                                "status","Successful",
                                "list of all questions with subject name" ,questionService.findBySubjectName(subjectName)
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
    public String test(){
        return "hii";
    }
}
