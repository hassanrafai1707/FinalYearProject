package com.FinalYearProject.FinalYearProject.Controller;

import com.FinalYearProject.FinalYearProject.DTO.*;
import com.FinalYearProject.FinalYearProject.Domain.Question;
import com.FinalYearProject.FinalYearProject.Domain.User;
import com.FinalYearProject.FinalYearProject.Service.QuestionService;
import com.FinalYearProject.FinalYearProject.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

//TODO fix user will not ask for mapped co only he will ask with subject name or subject code
@RequestMapping("${app.version}/teacher")
@RestController
public class TeacherRestController {
    @Autowired
    private UserService userService;
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
                                "all Question", questionList
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
    public ResponseEntity<?> addQuestion(@RequestBody Question question){
        Question savedQuestion=questionService.addQuestion(question);
        return ResponseEntity
                .ok(
                        Map.of(
                                "status","Successful",
                                "message","Question saved",
                                "Question saved",savedQuestion
                        )
                );
    }

    @PatchMapping("/updateUserEmailById")
    public ResponseEntity<?>updateUserEmailById(@RequestBody DtoForEmaiAndIdInRequest
                                                        dto){
        Long Id= dto.getId();
        String email= dto.getEmail();
        User upDatedUser= userService.updateUserEmailById(Id,email);
        return ResponseEntity.ok(
                Map.of(
                        "states","successful",
                        "updatedUser",upDatedUser
                )
        );
    }

    @PatchMapping("/updateUserEmailByEmail")
    public ResponseEntity<?> updateUserEmailByEmail(@RequestBody DtoForOldEmailAndNewEmailInRequest
                                                            dto){
        String newEmail= dto.getNewEmail();
        String oldEmail= dto.getOldEmail();
        User updatedUser=userService.updateUserEmailByEmail(oldEmail,newEmail);
        return ResponseEntity.ok(
                Map.of(
                        "states","successful",
                        "updatedUser",updatedUser
                )
        );
    }

    @PatchMapping("/updateUserPasswordByEmail")
    public ResponseEntity<?> updateUserPasswordByEmail(@RequestBody DtoForEmailAndPasswordInRequest
                                                               dto){
        String email= dto.getEmail();
        String newPassword= dto.getPassword();
        User updatedUser =userService.updateUserPasswordByEmail(email,newPassword);
        return ResponseEntity.ok(
                Map.of(
                        "states","successful",
                        "updatedUser",updatedUser
                )
        );
    }

    @PatchMapping("/updateUserPasswordById")
    public ResponseEntity<?> updateUserPasswordById(@RequestBody DtoFortUserIdAndPasswordInRequest
                                                            dto){
        Long Id=dto.getId();
        String password= dto.getPassword();
        User updatedUser = userService.updateUserPasswordById(Id,password);
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
