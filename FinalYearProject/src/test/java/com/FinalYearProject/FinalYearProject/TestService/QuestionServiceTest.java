package com.FinalYearProject.FinalYearProject.TestService;

import com.FinalYearProject.FinalYearProject.DTO.QuestionDto.QuestionDTO;
import com.FinalYearProject.FinalYearProject.Domain.Question;
import com.FinalYearProject.FinalYearProject.Domain.User;
import com.FinalYearProject.FinalYearProject.Exceptions.QuestionException.QuestionNotFoundException;
import com.FinalYearProject.FinalYearProject.Repository.QuestionRepository;
import com.FinalYearProject.FinalYearProject.Service.QuestionService;
import com.FinalYearProject.FinalYearProject.Service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Arrays;
import java.util.List;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// ExtendWith replaces the need for "MockitoAnnotations.openMocks(this)" in a setup method
@ExtendWith(MockitoExtension.class)
class QuestionServiceTest {

    // @Mock creates a "fake" repository. We control what it returns.
    @Mock
    private QuestionRepository questionRepository;

    @Mock
    private UserService userService;

    // @InjectMocks creates the real Service and injects the "fake" repositories into it.
    @InjectMocks
    private QuestionService questionService;

    // --------------------------------------------------------------------------------
    // CONSTANTS & HELPERS (The "DRY" Section)
    // --------------------------------------------------------------------------------
    // We define these here so we don't have to type "1L" or "JAVA" inside every single test.
    private static final Long ID = 1L;
    private static final String SUBJECT_JAVA = "JAVA";
    private static final String SUBJECT_CODE = "3140706";
    private static final String TEACHER_EMAIL = "abc@test.com";

    /**
     * Helper method to create a dummy Question object.
     * Instead of typing "new Question()..." in every test, we just call this.
     */
    private Question createQuestion() {
        Question q = new Question();
        q.setId(ID);
        q.setQuestionBody("Test Body");
        q.setCognitiveLevel("A");
        q.setMappedCO("CO1");
        q.setQuestionMarks(2);
        q.setInUse(false);
        return q;
    }

    /**
     * Helper method to create a dummy Teacher user.
     */
    private User createTeacher() {
        User u = new User();
        u.setRole("ROLE_TEACHER");
        u.setEmail(TEACHER_EMAIL);
        return u;
    }

    /**
     * Helper method to create a Page of questions for pagination tests.
     * This saves about 5 lines of code per test.
     */
    private Page<Question> createQuestionPage(int page, int size) {
        List<Question> questions = List.of(createQuestion());
        return new PageImpl<>(questions, PageRequest.of(page, size), questions.size());
    }

    // --------------------------------------------------------------------------------
    // TEST CASES
    // --------------------------------------------------------------------------------

    // ================= getQuestionById =================

    @Test
    void testGetQuestionByIdSuccess() {
        // Given: We have a valid question in the mock DB
        Question q = createQuestion();

        // Mocking: When repo is asked for ID 1, return our question
        when(questionRepository.findById(ID)).thenReturn(Optional.of(q));

        // When: We call the service method
        Question result = questionService.getQuestionById(ID);

        // Then: The ID should match what we put in
        assertEquals(ID, result.getId());
    }

    @Test
    void testGetQuestionByIdNotFound() {
        // Given: The repository cannot find the ID (returns empty)
        when(questionRepository.findById(ID)).thenReturn(Optional.empty());

        // When & Then: Expect a QuestionNotFoundException when we call the service
        assertThrows(QuestionNotFoundException.class,
                () -> questionService.getQuestionById(ID),
                "Should throw exception when ID not found");
    }

    // ================= findBySubjectName =================

    @Test
    void testFindBySubjectNameSuccess() {
        // Given: The repository finds a list of questions for "JAVA"
        when(questionRepository.findBySubjectName(SUBJECT_JAVA))
                .thenReturn(List.of(createQuestion()));

        // When: We search by subject name
        List<Question> result = questionService.findBySubjectName(SUBJECT_JAVA);

        // Then: We expect exactly 1 question in the list
        assertEquals(1, result.size());
    }

    @Test
    void testFindBySubjectNameNotFound() {
        // Given: The repository returns an empty list for "JAVA"
        when(questionRepository.findBySubjectName(SUBJECT_JAVA))
                .thenReturn(Collections.emptyList());

        // When & Then: The service should interpret empty list as an Exception
        assertThrows(QuestionNotFoundException.class,
                () -> questionService.findBySubjectName(SUBJECT_JAVA));
    }

    // ================= findBySubjectCode =================

    @Test
    void testFindBySubjectCodeSuccess() {
        // Given: Mock repo returning data for code "3140706"
        when(questionRepository.findBySubjectCode(SUBJECT_CODE))
                .thenReturn(List.of(createQuestion()));

        // When: calling service
        List<Question> result = questionService.findBySubjectCode(SUBJECT_CODE);

        // Then: Verify size
        assertEquals(1, result.size());
    }

    // ================= findByCreatedByUsingEmail =================

    @Test
    void testFindByCreatedByUsingEmailSuccess() {
        // Given: A valid teacher exists and has created questions
        User teacher = createTeacher();

        // Mock 1: User Service finds the teacher by email
        when(userService.findByEmail(TEACHER_EMAIL)).thenReturn(teacher);

        // Mock 2: Question Repo finds questions by that teacher
        when(questionRepository.findByCreatedBy(teacher))
                .thenReturn(List.of(createQuestion()));

        // When: We search by email
        List<Question> result = questionService.findByCreatedByUsingEmail(TEACHER_EMAIL);

        // Then: We get results
        assertEquals(1, result.size());
    }

    @Test
    void testFindByCreatedByUsingEmailNoQuestions() {
        // Given: A teacher exists, but has NO questions
        User teacher = createTeacher();

        when(userService.findByEmail(TEACHER_EMAIL)).thenReturn(teacher);
        when(questionRepository.findByCreatedBy(teacher))
                .thenReturn(Collections.emptyList()); // Return empty list

        // When & Then: Expect exception
        assertThrows(QuestionNotFoundException.class,
                () -> questionService.findByCreatedByUsingEmail(TEACHER_EMAIL));
    }

    // ================= deleteQuestionById =================

    @Test
    void testDeleteQuestionSuccess() {
        // Given: The question exists in the DB
        when(questionRepository.existsById(ID)).thenReturn(true);

        // When: We call delete
        questionService.deleteQuestionById(ID);

        // Then: Verify that the repository's deleteById method was actually called once
        verify(questionRepository).deleteById(ID);
    }

    @Test
    void testDeleteQuestionNotFound() {
        // Given: The question does NOT exist
        when(questionRepository.existsById(ID)).thenReturn(false);

        // When & Then: Trying to delete it should throw an exception
        assertThrows(QuestionNotFoundException.class,
                () -> questionService.deleteQuestionById(ID));
    }

    // ================= generateBySubjectCodeQuestion =================

    @Test
    void testGenerateBySubjectCodeQuestionSuccess() {
        // Given: Specific filter criteria
        // Note: usage of 'eq()' and 'any()' matchers for flexible mocking
        when(questionRepository.findValidQuestionsWithSubjectCode(
                eq(SUBJECT_CODE), any()))
                .thenReturn(List.of(createQuestion()));

        // When: Calling the generation logic
        List<Question> result = questionService.generateBySubjectCodeQuestion(
                SUBJECT_CODE,
                new String[]{"CO1"}, // Course Outcome
                1, 0, 0, // Difficulty counts
                1, 0     // Other params
        );

        // Then: We got our question back
        assertEquals(1, result.size());
    }

    @Test
    void testGenerateBySubjectCodeQuestionEmpty() {
        // Given: No questions match the criteria
        when(questionRepository.findValidQuestionsWithSubjectCode(
                anyString(), any()))
                .thenReturn(Collections.emptyList());

        // When & Then: Exception expected
        assertThrows(QuestionNotFoundException.class,
                () -> questionService.generateBySubjectCodeQuestion(
                        SUBJECT_CODE,
                        new String[]{"CO1"},
                        1, 0, 0,
                        1, 0
                ));
    }

    // ================= DTO & Pagination Tests =================

    @Test
    void testGetAllQuestionWithDTO() {
        // Given: Repo returns a list of entities
        when(questionRepository.findAll()).thenReturn(List.of(createQuestion()));

        // When: Service converts them to DTOs
        List<QuestionDTO> result = questionService.getAllQuestionWithDTO();

        // Then: Size matches
        assertEquals(1, result.size());
    }

    @Test
    void testGetAllQuestionsDTOPaged() {
        // Given: Pagination settings
        int pageNo = 1;
        int size = 100;

        // Mock: Return a "Page" object using our helper method
        when(questionRepository.findAll(any(Pageable.class)))
                .thenReturn(createQuestionPage(pageNo, size));

        // When: Call paged service
        Page<Question> result =
                questionService.getAllQuestionsPaged(pageNo, size);

        // Then: Verify page metadata and content
        assertNotNull(result);
        assertEquals(1, result.getContent().size());   // Content count
        assertEquals(size, result.getSize());          // Page size
    }

    @Test
    void testGetAllQuestionsDTOPagedImpl() {
        // Given: Pagination settings
        int pageNo = 1;
        int size = 100;

        // Mock: Return a "Page" object using our helper method
        when(questionRepository.findAll(any(Pageable.class)))
                .thenReturn(createQuestionPage(pageNo, size));

        // When: Call paged implementation service
        Page<QuestionDTO> result =
                questionService.getAllQuestionsDTOPagedImpl(pageNo, size);

        // Then: Verify
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
    }

    @Test
    void testGetQuestionDtoById() {
        // Given: A valid question entity
        Question question = createQuestion();

        // Mock: Repository finds the entity
        when(questionRepository.findById(ID)).thenReturn(Optional.of(question));

        // When: Call service to get DTO
        QuestionDTO result = questionService.getQuestionDtoById(ID);

        // Then: Verify DTO conversion is correct
        assertNotNull(result, "Returned DTO should not be null");
        assertEquals(ID, result.getId(), "Question ID should match");
    }

    @Test
    void testGetQuestionDtoById_NotFound() {
        // Given: A non-existent ID
        Long nonExistentId = 999L;

        // Mock: Repository returns empty
        when(questionRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // When & Then: Verify exception and message
        QuestionNotFoundException exception = assertThrows(
                QuestionNotFoundException.class,
                () -> questionService.getQuestionDtoById(nonExistentId)
        );

        // Optional: Check if the error message contains the ID we asked for
        assertTrue(exception.getMessage().contains(nonExistentId.toString()));
    }

    @Test
    void testGetQuestionDtoByIds() {
        // Given
        List<Long> ids = Arrays.asList(1L, 2L, 3L);

        // Create actual question entities that the repository will return
        Question q1 = createQuestion();
        q1.setId(1L);
        Question q2 = createQuestion();
        q2.setId(2L);
        Question q3 = createQuestion();
        q3.setId(3L);

        List<Question> questions = Arrays.asList(q1, q2, q3);

        // Mock the repository call
        when(questionRepository.findAllById(ids)).thenReturn(questions);

        // When
        List<QuestionDTO> result = questionService.getQuestionDtoByIds(ids);

        // Then
        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals(1L, result.get(0).getId());
        assertEquals(2L, result.get(1).getId());
        assertEquals(3L, result.get(2).getId());

        // Verify repository was called correctly
        verify(questionRepository).findAllById(ids);
    }
}