package com.FinalYearProject.FinalYearProject.Exceptions.UserEeceptions;

public class DuplicateEmailException extends RuntimeException {
    public DuplicateEmailException(String message) {
        super(message);
    }
}
