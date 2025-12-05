package com.FinalYearProject.FinalYearProject.Controller;

import com.FinalYearProject.FinalYearProject.DTO.DtoForEmailAndQuestionInRequest;
import com.FinalYearProject.FinalYearProject.Domain.Question;
import com.FinalYearProject.FinalYearProject.Service.QuestionService;
import com.FinalYearProject.FinalYearProject.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RequestMapping("${app.version}/teacher")
@RestController
public class TeacherRestController {

    @Autowired
    private QuestionService questionService;
    //TODO Use Question paper services here

    @GetMapping("/getAllQuestion")
    public ResponseEntity<?> getAllQuestion(){
        List<Question> questionList=questionService.getAllQuestion();
        return ResponseEntity
                .ok(
                        Map.of(
                                "status","Successful",
                                "all Users", questionList
                        )
                );
    }

    @GetMapping("/findByMappedCO")
    public ResponseEntity<?> findByMappedCO(@RequestBody Map<String,String> request){
        String MappedCO = request.get("mappedCO");
        List<Question> question=questionService.findByMappedCO(MappedCO);
        return ResponseEntity
                .ok(
                        Map.of(
                                "status","Successful",
                                "list of all questions with CO" ,question
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
                                "list of all questions with CO" ,questions
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

    @PostMapping("/addQuestion")
    public ResponseEntity<?> addQuestion(@RequestBody DtoForEmailAndQuestionInRequest dto){
        Question savedQuestion=questionService.addQuestion(
                dto.getQuestion(),
                dto.getEmail()
        );
        return ResponseEntity
                .ok(
                        Map.of(
                                "status","Successful",
                                "message","Question saved",
                                "Question saved",savedQuestion
                        )
                );
    }

    @GetMapping("/test")
    public String test(){
        return "hii";
    }
}
