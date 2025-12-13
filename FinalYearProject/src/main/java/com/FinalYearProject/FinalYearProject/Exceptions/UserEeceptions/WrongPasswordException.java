package com.FinalYearProject.FinalYearProject.Exceptions.UserEeceptions;

public class WrongPasswordException extends RuntimeException {
    public WrongPasswordException(String message) {
        super(message);
    }
}
