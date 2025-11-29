//package com.FinalYearProject.FinalYearProject.TestService;
//
//import com.FinalYearProject.FinalYearProject.Domain.Question;
//import com.FinalYearProject.FinalYearProject.Domain.User;
//import com.FinalYearProject.FinalYearProject.Repository.QuestionRepository;
//import com.FinalYearProject.FinalYearProject.Service.QuestionService;
//import com.FinalYearProject.FinalYearProject.Service.UserService;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.mockito.*;
//import java.util.*;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.Mockito.*;
//
//class QuestionServiceTest {
//
//    @Mock
//    private QuestionRepository questionRepository;
//
//    @Mock
//    private UserService userService;
//
//    @InjectMocks
//    private QuestionService questionService;
//
//    @BeforeEach
//    void setup() {
//        MockitoAnnotations.openMocks(this);
//    }
//
//    // ============= TEST : getQuestionById ==================
//    @Test
//    void testGetQuestionByIdSuccess() {
//        Question q = new Question();
//        q.setId(1L);
//
//        when(questionRepository.findById(1L)).thenReturn(Optional.of(q));
//
//        Question result = questionService.getQuestionById(1L);
//
//        assertEquals(1L, result.getId());
//        verify(questionRepository, times(1)).findById(1L);
//    }
//
//    @Test
//    void testGetQuestionByIdNotFound() {
//        when(questionRepository.findById(1L)).thenReturn(Optional.empty());
//        assertThrows(RuntimeException.class, () -> questionService.getQuestionById(1L));
//    }
//
//    // ============= TEST : findByMappedCO ==================
//    @Test
//    void testFindByMappedCOSuccess() {
//        List<Question> list = List.of(new Question());
//        when(questionRepository.findByMappedCO("CO1")).thenReturn(list);
//
//        assertEquals(1, questionService.findByMappedCO("CO1").size());
//    }
//
//    // ============= TEST : findBySubjectName ==================
//    @Test
//    void testFindBySubjectNameSuccess() {
//        List<Question> list = List.of(new Question());
//        when(questionRepository.findBySubjectName("JAVA")).thenReturn(list);
//
//        assertEquals(1, questionService.findBySubjectName("JAVA").size());
//    }
//
//    // ============= TEST : findBySubjectCode ==================
//    @Test
//    void testFindBySubjectCodeSuccess() {
//        List<Question> list = List.of(new Question());
//        when(questionRepository.findBySubjectCode("3140706")).thenReturn(list);
//
//        assertEquals(1, questionService.findBySubjectCode("3140706").size());
//    }
//
//    // ============= TEST : findByCognitiveLevel ==================
//    @Test
//    void testFindByCognitiveLevelSuccess() {
//        List<Question> list = List.of(new Question());
//        when(questionRepository.findByCognitiveLevel("L")).thenReturn(list);
//
//        assertEquals(1, questionService.findByCognitiveLevel("L").size());
//    }
//
//    // ============= TEST : findByCreatedByUsingEmail ==================
//    @Test
//    void testFindByCreatedBySuccess() {
//        User user = new User();
//        user.setEmail("abc@test.com");
//
//        List<Question> list = List.of(new Question());
//
//        when(userService.existsByEmail("abc@test.com")).thenReturn(true);
//        when(questionRepository.findByCreatedBy(user)).thenReturn(list);
//
//        assertEquals(1, questionService.findByCreatedByUsingEmail(user).size());
//    }
//
//    @Test
//    void testFindByCreatedByUserDoesNotExist() {
//        User user = new User();
//        user.setEmail("no@test.com");
//
//        when(userService.existsByEmail(anyString())).thenReturn(false);
//
//        assertThrows(RuntimeException.class,
//                () -> questionService.findByCreatedByUsingEmail(user));
//    }
//
//    // ============= TEST : addQuestion ==================
//    @Test
//    void testAddQuestion() {
//        Question q = new Question();
//        q.setInUse(true);
//
//        when(questionRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);
//
//        Question result = questionService.addQuestion(q);
//
//        assertFalse(result.getInUse()); // must be false
//        verify(questionRepository).save(q);
//    }
//
//    // ============= TEST : deleteQuestionById ==================
//    @Test
//    void testDeleteQuestionSuccess() {
//        when(questionRepository.existsById(1L)).thenReturn(true);
//
//        questionService.deleteQuestionById(1L);
//
//        verify(questionRepository, times(1)).deleteById(1L);
//    }
//
//    @Test
//    void testDeleteQuestionNotFound() {
//        when(questionRepository.existsById(1L)).thenReturn(false);
//
//        assertThrows(RuntimeException.class,
//                () -> questionService.deleteQuestionById(1L));
//    }
//
//    // ============= TEST : generateQuestion ==================
//    @Test
//    void testGenerateQuestionSuccess() {
//        Question q1 = new Question();
//        q1.setCognitiveLevel("L");
//        q1.setMappedCO("CO1");
//        q1.setQuestionMarks(2);
//        q1.setInUse(false);
//
//        when(questionRepository.findAll()).thenReturn(List.of(q1));
//
//        List<Question> result = questionService.generateQuestion(
//                new String[]{"CO1"},
//                1, 0, 0,
//                1, 0,
//                2);
//
//        assertEquals(1, result.size());
//    }
//
//    @Test
//    void testGenerateQuestionEmptyResult() {
//        when(questionRepository.findAll()).thenReturn(Collections.emptyList());
//
//        List<Question> result = questionService.generateQuestion(
//                new String[]{"CO1"},
//                1, 0, 0,
//                1, 0,
//                2);
//
//        assertTrue(result.isEmpty());
//    }
//}
