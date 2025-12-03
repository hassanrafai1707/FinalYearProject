package com.FinalYearProject.FinalYearProject.Exceptions;

import java.time.LocalDateTime;

public class ErrorResponse {
    private String message;
    private int status;
    private LocalDateTime timestamp;
    private String path;

    public ErrorResponse(String message, int status, LocalDateTime timestamp, String path) {
        this.message = message;
        this.status = status;
        this.timestamp = timestamp;
        this.path = path;
    }

    // getters and setters
    public String getMessage() { return message; }
    public int getStatus() { return status; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public String getPath() { return path; }

    public void setMessage(String message) { this.message = message; }
    public void setStatus(int status) { this.status = status; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    public void setPath(String path) { this.path = path; }
}
