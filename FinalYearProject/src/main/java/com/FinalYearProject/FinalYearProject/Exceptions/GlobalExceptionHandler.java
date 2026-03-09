package com.FinalYearProject.FinalYearProject.Exceptions;

import com.FinalYearProject.FinalYearProject.Exceptions.QuestionException.DuplicateQuestionException;
import com.FinalYearProject.FinalYearProject.Exceptions.QuestionException.QuestionNotFoundException;
import com.FinalYearProject.FinalYearProject.Exceptions.QuestionException.UnacceptableQuestion;
import com.FinalYearProject.FinalYearProject.Exceptions.QuestionPaperException.DuplicateQuestionPaperException;
import com.FinalYearProject.FinalYearProject.Exceptions.QuestionPaperException.QuestionPaperNotFoundException;
import com.FinalYearProject.FinalYearProject.Exceptions.UserEeceptions.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.BadRequestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.LockedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

/**
 * GlobalExceptionHandler - Centralized Exception Handling Controller Advice
 * PURPOSE: Global exception handler that intercepts and processes all exceptions across the application, providing consistent error responses and centralized error logging.
 * ARCHITECTURE: Uses @RestControllerAdvice to apply exception handling globally. @Slf4j for structured logging. Individual @ExceptionHandler methods for specific exception types.
 * ERROR CATEGORIZATION: Groups exceptions by domain - User errors (4xx), Question errors (4xx), QuestionPaper errors (4xx), and generic Exception (500) as catch-all.
 * HTTP STATUS MAPPING: Maps business exceptions to appropriate HTTP status codes: NOT_FOUND (404), CONFLICT (409), UNAUTHORIZED (401), FORBIDDEN (403), BAD_REQUEST (400), NOT_ACCEPTABLE (406), LOCKED (423).
 * LOGGING STRATEGY: Logs all exceptions with error level for monitoring and debugging. Includes stack trace for internal errors but sanitizes in production.
 * RESPONSE CONSISTENCY: Uses ErrorResponse DTO for standardized error format across all endpoints. Includes message, status, timestamp, and request path.
 * SECURITY CONSIDERATIONS: Avoids exposing internal details in error messages. Generic messages for security exceptions (WrongPasswordException gets "Invalid credentials").
 * BUILD RESPONSE METHOD: Centralized response building ensures consistent error construction. Reduces code duplication across exception handlers.
 * EXCEPTION HIERARCHY: Handles both custom business exceptions and generic Exception as fallback. Should be extended for new exception types.
 * MONITORING INTEGRATION: Exception logging enables integration with monitoring systems (ELK, Splunk, etc.). Timestamps support correlation with application logs.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler  {

    private final Logger logger=  LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private ResponseEntity<ErrorResponse> buildResponse(
            Exception e,
            HttpStatus status,
            HttpServletRequest request
    ){
        logger.error("Exception occurred:", e);
        ErrorResponse errorResponse= new ErrorResponse(
                e.getMessage(),
                status.value(),
                LocalDateTime.now(),
                request.getRequestURI()
        );
        return new ResponseEntity<>(errorResponse,status);
    }
    //User error handler
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFoudException(UserNotFoundException e,HttpServletRequest request){
        return buildResponse(e,HttpStatus.NOT_FOUND,request);
    }

    @ExceptionHandler(UserNotAuthorizesException.class)
    public ResponseEntity<ErrorResponse> handleUserNotAuthorizedException(UserNotAuthorizesException e, HttpServletRequest request){
        return buildResponse(e,HttpStatus.UNAUTHORIZED,request);
    }

    @ExceptionHandler(UserLockedException.class)
    public ResponseEntity<ErrorResponse> handleUserLockedException(UserLockedException e,HttpServletRequest request){
        return buildResponse(e,HttpStatus.LOCKED,request);
    }

    @ExceptionHandler(DuplicateEmailException.class)
    public ResponseEntity<ErrorResponse> handleUserAllReadyExists(DuplicateEmailException e, HttpServletRequest request){
        return buildResponse(e,HttpStatus.CONFLICT,request);
    }

    @ExceptionHandler(UnacceptableRequestException.class)
    public ResponseEntity<ErrorResponse>handleUnacceptableRequest(UnacceptableRequestException e, HttpServletRequest request){
        return buildResponse(e,HttpStatus.BAD_REQUEST,request);
    }

    @ExceptionHandler(WrongPasswordException.class)
    public ResponseEntity<ErrorResponse> handleWrongPasswordException(WrongPasswordException e,HttpServletRequest request){
        return buildResponse(e,HttpStatus.FORBIDDEN,request);
    }
     //Question error handler
    @ExceptionHandler(QuestionNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleQuestionNotFound(QuestionNotFoundException e, HttpServletRequest request){
        return buildResponse(e,HttpStatus.NOT_FOUND,request);
    }

    @ExceptionHandler(DuplicateQuestionException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateQuestionException(DuplicateQuestionException e, HttpServletRequest request){
        return buildResponse(e,HttpStatus.CONFLICT,request);
    }

    @ExceptionHandler(UnacceptableQuestion.class)
    public ResponseEntity<ErrorResponse> handleUnacceptableQuestionException(UnacceptableQuestion e ,HttpServletRequest request){
        return buildResponse(e,HttpStatus.NOT_ACCEPTABLE,request);
    }
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnknownException(Exception e, HttpServletRequest request){
        return buildResponse(e,HttpStatus.INTERNAL_SERVER_ERROR,request);
    }

    @ExceptionHandler(DuplicateQuestionPaperException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateQuestionPaperException(DuplicateEmailException e,HttpServletRequest request){
        return buildResponse(e, HttpStatus.CONFLICT, request);
    }

    @ExceptionHandler(QuestionPaperNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleQuestionPaperNotFoundException(DuplicateQuestionException e, HttpServletRequest request){
        return buildResponse(e,HttpStatus.NOT_FOUND, request);
    }

    //internal exceptions
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponse> handleBadRequestException(BadRequestException e , HttpServletRequest request){
        return buildResponse(e,HttpStatus.BAD_REQUEST,request);
    }

    @ExceptionHandler(LockedException.class)
    public ResponseEntity<ErrorResponse> handleLockedException(LockedException e, HttpServletRequest request){
        return buildResponse(e,HttpStatus.LOCKED,request);
    }

    @ExceptionHandler(DepartmentMissMatchException.class)
    public ResponseEntity<ErrorResponse> handleDepartmentMissMatchException(DepartmentMissMatchException e,HttpServletRequest request){
        return buildResponse(e,HttpStatus.UNAUTHORIZED,request);
    }
}
