package com.FinalYearProject.FinalYearProject.Exceptions.QuestionException;

/**
 * DuplicateQuestionException - Question Bank Integrity Exception
 * PURPOSE: Custom runtime exception thrown when attempting to add a duplicate question to the question bank, ensuring question uniqueness and preventing data redundancy.
 * EXCEPTION TYPE: Unchecked (RuntimeException) following Spring convention for business rule violations that should not be caught and handled locally.
 * TRIGGER CONDITIONS: Thrown when question title or content fingerprint matches existing question. Used in QuestionService.addQuestion() and similar operations.
 * UNIQUENESS ENFORCEMENT: Maintains question bank integrity by preventing identical or near-identical questions. Supports quality control and prevents assessment redundancy.
 * ERROR HANDLING: Typically caught by global @ControllerAdvice exception handler and converted to appropriate HTTP error response (409 Conflict or 400 Bad Request).
 * MESSAGE CONTENT: Should include details about duplicate detection - e.g., "Question with title '...' already exists" or "Similar question found with ID: 123".
 * USAGE CONTEXT: Used in teacher controller addQuestion endpoint. Also applicable in bulk import operations and question synchronization scenarios.
 * INTEGRATION: Extends RuntimeException for seamless Spring exception translation. Works with Spring's ResponseEntityExceptionHandler for consistent error responses.
 * ALTERNATIVES: Consider QuestionAlreadyExistsException for semantic clarity. Could include fields for duplicate question ID for reference.
 */
public class DuplicateQuestionException extends RuntimeException {
    public DuplicateQuestionException(String message) {
        super(message);
    }
}
