package com.FinalYearProject.FinalYearProject.Controller;

import com.FinalYearProject.FinalYearProject.DTO.QuestionDto.*;
import com.FinalYearProject.FinalYearProject.Domain.Question;
import com.FinalYearProject.FinalYearProject.Domain.User;
import com.FinalYearProject.FinalYearProject.Service.QuestionPaperService;
import com.FinalYearProject.FinalYearProject.Service.QuestionService;
import com.FinalYearProject.FinalYearProject.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.web.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RequestMapping("${app.version}/teacher")
@RestController
public class TeacherRestController {
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
    public Page<Question>  getAllQuestionsPaged(
            @RequestParam(value = "pageNo", defaultValue = "0") int pageNo,
            @RequestParam(value = "size",defaultValue = "100") int size
    ){
        return questionService.getAllQuestionsPaged(pageNo,size);
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

    @GetMapping("/generateBySubjectCodeQuestionPaper")
    public ResponseEntity<?> generateBySubjectCodeQuestionPaper(@RequestBody DtoForSubjectCodeAndMappedCOs_ARU_And_2_4_Marks dto){
        List<Question> generatedQuestionPaper=questionService.generateBySubjectCodeQuestion(
                dto.getSubjectCode(),
                dto.getMappedCOs(),
                dto.getNumberOfCognitiveLevel_A(),
                dto.getNumberOfCognitiveLevel_R(),
                dto.getNumberOfCognitiveLevel_U(),
                dto.getMaxNumberOf2Marks(),
                dto.getMaxNumberOf4Marks()
        );
        return ResponseEntity
                .ok(
                        Map.of(
                                "status","Successful",
                                "generated Question Paper",generatedQuestionPaper
                        )
                );
    }

    @GetMapping("/generateBySubjectNameAndQuestionPaper")
    public ResponseEntity<?> generateBySubjectAndQuestionPaper(@RequestBody DtoForSubjectNameAndMappedCOs_ARU_And_2_4_Marks dto){
        List<Question> generatedQuestionPaper=questionService.generateBySubjectNameQuestion(
                dto.getSubjectName(),
                dto.getMappedCOs(),
                dto.getNumberOfCognitiveLevel_A(),
                dto.getNumberOfCognitiveLevel_R(),
                dto.getNumberOfCognitiveLevel_U(),
                dto.getMaxNumberOf2Marks(),
                dto.getMaxNumberOf4Marks()
        );
        return ResponseEntity
                .ok(
                        Map.of(
                                "status","Successful",
                                "generated Question Paper",generatedQuestionPaper
                        )
                );
    }

    @GetMapping("/getYourQuestion")
    public ResponseEntity<?> getYourQuestion(){
        List<Question> allQuestionPapersGeneratedByUser=questionService.getAllQuestionsByCurrentUser();
        return ResponseEntity
                .ok(
                        Map.of(
                                "status","Successful",
                                "all your questions " ,allQuestionPapersGeneratedByUser
                        )
                );
    }

    @GetMapping("/getYourQuestionPaged")
    public Page<Question> getYourQuestion(
            @RequestParam(value = "pageNo",defaultValue = "0")int pageNo,
            @RequestParam(value = "size",defaultValue = "100")int size
    ){
        return questionService.getAllQuestionsByCurrentUser(pageNo, size);
    }

    @PostMapping("/approveGeneratedQuestionPaper")
    public ResponseEntity<?> approveGeneratedQuestionPaper(){
        return ResponseEntity.ok(
                Map.of(
                        "status","Successful"
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

    @DeleteMapping("/deleteQuestionById")
    public ResponseEntity<?> deleteQuestionById(@RequestBody Map<String,Long> request){
        questionService.deleteQuestionById(request.get("id"));
        return ResponseEntity
                .ok(
                        Map.of(
                                "states","successful"
                        )
                );
    }

    @DeleteMapping("/deleteQestionByQuestionBody")
    public ResponseEntity<?> deleteQuestionByQuestionBody(@RequestBody Map<String,String> request){
        questionService.deleteQuestionByQuestionBody(request.get("questionBody"));
        return ResponseEntity.ok(
                Map.of(
                        "status","successful"
                )
        );
    }

    @GetMapping("/test")
    public String test(){
        return "hii";
    }
}
