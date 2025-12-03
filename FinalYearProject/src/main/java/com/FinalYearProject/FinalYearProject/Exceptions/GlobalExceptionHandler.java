package com.FinalYearProject.FinalYearProject.Exceptions;

import com.FinalYearProject.FinalYearProject.Exceptions.QuestionException.QuestionExistsException;
import com.FinalYearProject.FinalYearProject.Exceptions.QuestionException.QuestionNotFoundException;
import com.FinalYearProject.FinalYearProject.Exceptions.UserEeceptions.EmailAllReadyExistsException;
import com.FinalYearProject.FinalYearProject.Exceptions.UserEeceptions.UserLockedException;
import com.FinalYearProject.FinalYearProject.Exceptions.UserEeceptions.UserNotAuthorizesException;
import com.FinalYearProject.FinalYearProject.Exceptions.UserEeceptions.UserNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;


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

    @ExceptionHandler(EmailAllReadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleUserAllReadyExists(EmailAllReadyExistsException e, HttpServletRequest request){
        return buildResponse(e,HttpStatus.CONFLICT,request);
    }

     //Question error handler
    @ExceptionHandler(QuestionNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleQuestionNotFound(QuestionNotFoundException e, HttpServletRequest request){
        return buildResponse(e,HttpStatus.NOT_FOUND,request);
    }

    @ExceptionHandler(QuestionExistsException.class)
    public ResponseEntity<ErrorResponse> handleQuestionExists(QuestionExistsException e, HttpServletRequest request){
        return buildResponse(e,HttpStatus.CONFLICT,request);
    }
}
