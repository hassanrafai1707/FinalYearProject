package com.FinalYearProject.FinalYearProject.TestService;

import com.FinalYearProject.FinalYearProject.DTO.QuestionDto.QuestionDTO;
import com.FinalYearProject.FinalYearProject.Domain.Question;
import com.FinalYearProject.FinalYearProject.Domain.User;
import com.FinalYearProject.FinalYearProject.Exceptions.QuestionException.QuestionNotFoundException;
import com.FinalYearProject.FinalYearProject.Repository.QuestionRepository;
import com.FinalYearProject.FinalYearProject.Service.QuestionService;
import com.FinalYearProject.FinalYearProject.Service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;


import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class QuestionServiceTest {

    @Mock
    private QuestionRepository questionRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private QuestionService questionService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    // ================= getQuestionById =================
    @Test
    void testGetQuestionByIdSuccess() {
        Question q = new Question();
        q.setId(1L);

        when(questionRepository.findById(1L)).thenReturn(Optional.of(q));

        Question result = questionService.getQuestionById(1L);

        assertEquals(1L, result.getId());
    }

    @Test
    void testGetQuestionByIdNotFound() {
        when(questionRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(QuestionNotFoundException.class,
                () -> questionService.getQuestionById(1L));
    }

    // ================= findBySubjectName =================
    @Test
    void testFindBySubjectNameSuccess() {
        when(questionRepository.findBySubjectName("JAVA"))
                .thenReturn(List.of(new Question()));

        assertEquals(1, questionService.findBySubjectName("JAVA").size());
    }

    @Test
    void testFindBySubjectNameNotFound() {
        when(questionRepository.findBySubjectName("JAVA"))
                .thenReturn(Collections.emptyList());

        assertThrows(QuestionNotFoundException.class,
                () -> questionService.findBySubjectName("JAVA"));
    }

    // ================= findBySubjectCode =================
    @Test
    void testFindBySubjectCodeSuccess() {
        when(questionRepository.findBySubjectCode("3140706"))
                .thenReturn(List.of(new Question()));

        assertEquals(1, questionService.findBySubjectCode("3140706").size());
    }

    // ================= findByCreatedByUsingEmail =================
    @Test
    void testFindByCreatedByUsingEmailSuccess() {
        User teacher = new User();
        teacher.setRole("ROLE_TEACHER");

        when(userService.findByEmail("abc@test.com")).thenReturn(teacher);
        when(questionRepository.findByCreatedBy(teacher))
                .thenReturn(List.of(new Question()));

        assertEquals(1,
                questionService.findByCreatedByUsingEmail("abc@test.com").size());
    }

    @Test
    void testFindByCreatedByUsingEmailNoQuestions() {
        User teacher = new User();
        teacher.setRole("ROLE_TEACHER");

        when(userService.findByEmail("abc@test.com")).thenReturn(teacher);
        when(questionRepository.findByCreatedBy(teacher))
                .thenReturn(Collections.emptyList());

        assertThrows(QuestionNotFoundException.class,
                () -> questionService.findByCreatedByUsingEmail("abc@test.com"));
    }

    // ================= addQuestion =================
//    @Test
//    void testAddQuestionSuccess() {
//        // mock security context
//        Authentication auth = mock(Authentication.class);
//        SecurityContext context = mock(SecurityContext.class);
//
//        when(auth.getName()).thenReturn("teacher@test.com");
//        when(context.getAuthentication()).thenReturn(auth);
//        SecurityContextHolder.setContext(context);
//
//        User teacher = new User();
//        teacher.setRole("ROLE_TEACHER");
//
//        when(userService.findByEmail("teacher@test.com")).thenReturn(teacher);
//        when(questionRepository.existsByQuestionTitle(anyString()))
//                .thenReturn(false);
//        when(questionRepository.save(any()))
//                .thenAnswer(i -> i.getArgument(0));
//
//        Question q = new Question();
//        q.setQuestionBody("What is JVM?");
//        q.setQuestionMarks(2);
//
//        Question result = questionService.addQuestion(q);
//
//        assertFalse(result.getInUse());
//        verify(questionRepository).save(q);
//    }

    // ================= deleteQuestionById =================
    @Test
    void testDeleteQuestionSuccess() {
        when(questionRepository.existsById(1L)).thenReturn(true);

        questionService.deleteQuestionById(1L);

        verify(questionRepository).deleteById(1L);
    }

    @Test
    void testDeleteQuestionNotFound() {
        when(questionRepository.existsById(1L)).thenReturn(false);

        assertThrows(QuestionNotFoundException.class,
                () -> questionService.deleteQuestionById(1L));
    }

    // ================= generateBySubjectCodeQuestion =================
    @Test
    void testGenerateBySubjectCodeQuestionSuccess() {
        Question q = new Question();
        q.setCognitiveLevel("A");
        q.setMappedCO("CO1");
        q.setQuestionMarks(2);
        q.setInUse(false);

        when(questionRepository.findValidQuestionsWithSubjectCode(
                eq("3140706"), any()))
                .thenReturn(List.of(q));

        List<Question> result =
                questionService.generateBySubjectCodeQuestion(
                        "3140706",
                        new String[]{"CO1"},
                        1, 0, 0,
                        1, 0
                );

        assertEquals(1, result.size());
    }

    @Test
    void testGenerateBySubjectCodeQuestionEmpty() {
        when(questionRepository.findValidQuestionsWithSubjectCode(
                anyString(), any()))
                .thenReturn(Collections.emptyList());

        assertThrows(QuestionNotFoundException.class,
                () -> questionService.generateBySubjectCodeQuestion(
                        "3140706",
                        new String[]{"CO1"},
                        1, 0, 0,
                        1, 0
                ));
    }

    @Test
    void testGetAllQuestionWithDTO(){
        Question q = new Question();
        q.setId(1L);

        when(questionRepository.findAll()).thenReturn(List.of(q));
        List<QuestionDTO> result=questionService.getAllQuestionWithDTO();
        assertEquals(1,result.size());
    }

    @Test
    void testGetAllQuestionsDTOPaged() {
        int pageNo = 1;
        int size = 100;

        // ---- test data ----
        Question q = new Question();
        q.setId(1L);

        Page<Question> page =
                new PageImpl<>(
                        List.of(q),
                        PageRequest.of(pageNo, size),
                        1
                );

        // ---- mock repository (IMPORTANT) ----
        when(questionRepository.findAll(any(Pageable.class)))
                .thenReturn(page);

        // ---- execute ----
        Page<QuestionDTO> result =
                questionService.getAllQuestionsDTOPaged(pageNo, size);

        // ---- assertions ----
        assertNotNull(result);
        assertEquals(1, result.getContent().size());   // content count
        assertEquals(size, result.getSize());          // page size
        assertEquals(pageNo, result.getNumber());      // page number
    }

    @Test
    void testGetAllQuestionsDTOPagedImpl(){
        int pageNo = 1;
        int size = 100;

        // ---- test data ----
        Question q = new Question();
        q.setId(1L);

        Page<Question> page =
                new PageImpl<>(
                        List.of(q),
                        PageRequest.of(pageNo, size),
                        1
                );

        // ---- mock repository (IMPORTANT) ----
        when(questionRepository.findAll(any(Pageable.class)))
                .thenReturn(page);

        // ---- execute ----
        Page<QuestionDTO> result =
                questionService.getAllQuestionsDTOPagedImpl(pageNo, size);

        // ---- assertions ----
        assertNotNull(result);
        assertEquals(1, result.getContent().size());   // content count
        assertEquals(size, result.getSize());          // page size
        assertEquals(pageNo, result.getNumber());      // page number
    }
}
