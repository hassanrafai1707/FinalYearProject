package com.FinalYearProject.FinalYearProject.Util;

import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.Map;

public class ResponseUtility {
    private ResponseUtility(){};
    public static ResponseEntity<?> responseTemplateForSingleData(String status , Object data, String message ,int statusCode){
        return  ResponseEntity
                .status(statusCode)
                .body(
                        Map.of(
                            "status", status,
                            "data",data,
                            "message",message,
                            "time", LocalDateTime.now()
                        )
                );
    }

    public static ResponseEntity<?> responseTemplateForMultipleData(String status , Object[] data, String message ,int statusCode){
        return  ResponseEntity
                .status(statusCode)
                .body(
                        Map.of(
                                "status", status,
                                "data",data,
                                "message",message,
                                "time", LocalDateTime.now()
                        )
                );
    }
}
