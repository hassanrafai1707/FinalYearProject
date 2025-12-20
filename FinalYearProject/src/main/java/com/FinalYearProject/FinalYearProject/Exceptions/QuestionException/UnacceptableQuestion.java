package com.FinalYearProject.FinalYearProject.Exceptions.QuestionException;

/**
 * UnacceptableQuestionException - Question Validation Exception
 * PURPOSE: Custom runtime exception thrown when a question fails validation checks or business rules, indicating the question cannot be accepted into the question bank.
 * VALIDATION FAILURE: Covers various validation scenarios: missing required fields, invalid cognitive level, incorrect mark values, improper formatting, or content policy violations.
 * EXCEPTION TYPE: Unchecked (RuntimeException) as validation failures are typically unrecoverable without user intervention and should bubble up to controller layer.
 * TRIGGER CONDITIONS: Thrown during question creation/update when business rules are violated. Used in QuestionService validation logic before persisting questions.
 * ERROR HANDLING: Typically results in HTTP 400 Bad Request response. Global exception handler should provide detailed validation error message to client.
 * MESSAGE CONTENT: Should specify which validation failed - e.g., "Cognitive level must be A, R, or U", "Question marks must be 2 or 4", "Question body cannot be empty".
 * USAGE CONTEXT: Used in teacher controller addQuestion endpoint and question import/update operations. Also applicable in bulk question validation.
 * BUSINESS RULE ENFORCEMENT: Ensures question bank quality by rejecting questions that don't meet institutional standards. Maintains consistency for paper generation algorithms.
 * INTEGRATION: Extends RuntimeException for Spring compatibility. Can be enhanced with validation error codes or field-specific error details for better client feedback.
 */
public class UnacceptableQuestion extends RuntimeException {
    public UnacceptableQuestion(String message) {
        super(message);
    }
}
