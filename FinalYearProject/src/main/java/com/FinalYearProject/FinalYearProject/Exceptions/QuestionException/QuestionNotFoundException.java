package com.FinalYearProject.FinalYearProject.Exceptions.QuestionException;

/**
 * QuestionNotFoundException - Question Bank Lookup Exception
 * PURPOSE: Custom runtime exception thrown when a requested question cannot be found in the question bank, indicating invalid question reference or missing data.
 * EXCEPTION TYPE: Unchecked (RuntimeException) following Spring practices for "not found" scenarios that typically result in 404 HTTP responses.
 * TRIGGER CONDITIONS: Thrown when question lookup by ID, title, or other identifier returns null/empty. Used in QuestionService methods like getQuestionById(), deleteQuestionById(), etc.
 * ERROR HANDLING: Caught by global exception handler (@ControllerAdvice) and converted to HTTP 404 Not Found response with appropriate error message.
 * MESSAGE CONTENT: Should include identifying information for debugging - e.g., "Question with ID 123 not found" or "No questions found for subject code CS101".
 * USAGE CONTEXT: Used across all role controllers (student, teacher, supervisor) when querying non-existent questions. Also in paper generation when referenced questions are missing.
 * INTEGRATION: Extends RuntimeException for Spring compatibility. Works with Spring's response status annotations (@ResponseStatus) for automatic HTTP status mapping.
 * ALTERNATIVE APPROACHES: Could use Optional return types instead of exceptions for query methods. However, exceptions provide clearer control flow for "must exist" scenarios.
 */
public class QuestionNotFoundException extends RuntimeException {
    public QuestionNotFoundException(String message) {
        super(message);
    }
}
