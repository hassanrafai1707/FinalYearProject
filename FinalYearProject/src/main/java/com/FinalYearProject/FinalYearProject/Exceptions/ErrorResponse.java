package com.FinalYearProject.FinalYearProject.Exceptions;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * ErrorResponse - Standardized API Error Response DTO
 * PURPOSE: Data Transfer Object for consistent error responses across all API endpoints. Provides standardized structure for client error handling and debugging.
 * RESPONSE STRUCTURE: message - human-readable error description. status - HTTP status code. timestamp - error occurrence time. path - requested API endpoint path.
 * CONSISTENCY BENEFIT: Ensures all controllers and exception handlers return errors in same format. Enables client-side error handling with predictable structure.
 * DEBUGGING SUPPORT: timestamp helps correlate errors with logs. path identifies which endpoint failed. message provides actionable information for developers.
 * HTTP STATUS MAPPING: status field mirrors HTTP response status for clarity. Allows clients to check status both in HTTP headers and response body.
 * TIMESTAMP USAGE: LocalDateTime provides error timing for audit trails and debugging. Should use server timezone or UTC for consistency.
 * LOMBOK INTEGRATION: @Getter/@Setter reduce boilerplate while maintaining immutability through constructor. Could use @Builder for flexible construction.
 * SERIALIZATION: Clean JSON structure suitable for REST APIs. Compatible with Spring's ResponseEntity for error responses.
 * EXTENSION POINTS: Could add errorCode for programmatic handling, stackTrace for development, correlationId for distributed tracing, or validationErrors for form validation.
 */
@Getter
@Setter
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

    public String getMessage() {
        return message;
    }
    public int getStatus() {
        return status;
    }
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    public String getPath() {
        return path;
    }

    public void setMessage(String message) {
        this.message = message;
    }
    public void setStatus(int status) {
        this.status = status;
    }
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
    public void setPath(String path) {
        this.path = path;
    }
}
