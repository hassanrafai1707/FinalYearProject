package com.FinalYearProject.FinalYearProject.Exceptions.QuestionPaperException;

/**
 * DuplicateQuestionPaperException - Exam Paper Integrity Exception
 * PURPOSE: Custom runtime exception thrown when attempting to create a question paper with duplicate identifying attributes (title, fingerprint), preventing redundant exam papers.
 * EXCEPTION TYPE: Unchecked (RuntimeException) for business rule violations that should propagate to controller layer for proper error response handling.
 * TRIGGER CONDITIONS: Thrown when question paper title already exists or content fingerprint matches existing paper. Used in QuestionPaperService.addQuestionPaper().
 * UNIQUENESS ENFORCEMENT: Maintains exam paper uniqueness to avoid confusion and ensure each paper has distinct identity. Critical for exam administration and tracking.
 * FINGERPRINT COLLISION: Content-based fingerprinting detects semantically similar papers even with different titles, preventing near-duplicate paper creation.
 * ERROR HANDLING: Results in HTTP 409 Conflict or 400 Bad Request response. Global exception handler provides meaningful error message about duplicate detection.
 * MESSAGE CONTENT: Should include duplicate details - e.g., "Question paper with title 'Midterm 2023' already exists (ID: 456)" or "Similar paper found with fingerprint match".
 * USAGE CONTEXT: Used in teacher controller approveGeneratedQuestionPaper endpoint. Also applicable in paper import and bulk creation scenarios.
 * INTEGRATION: Extends RuntimeException for Spring compatibility. Works with Spring's exception translation mechanism for consistent API error responses.
 */
public class DuplicateQuestionPaperException extends RuntimeException {
    public DuplicateQuestionPaperException(String message) {
        super(message);
    }
}
