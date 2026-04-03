package com.FinalYearProject.FinalYearProject.Exceptions;

public class ToManyRequests extends RuntimeException {
    public ToManyRequests(String message) {
        super(message);
    }
}
