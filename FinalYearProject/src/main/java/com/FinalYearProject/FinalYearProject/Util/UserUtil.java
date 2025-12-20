package com.FinalYearProject.FinalYearProject.Util;

import com.FinalYearProject.FinalYearProject.Domain.UserPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * UserUtil - Security Context Utility Class for Current User Access
 * PURPOSE: Provides convenient access to current authenticated user's security context information. Abstracts Spring Security context retrieval.
 * SECURITY CONTEXT ACCESS: getUserAuthentication() retrieves UserPrincipal from Spring Security's SecurityContextHolder. Returns current authenticated user's details.
 * TYPE SAFETY: Casts Authentication principal to UserPrincipal type. Assumes proper Spring Security configuration and authentication flow.
 * USAGE SIMPLIFICATION: Eliminates repetitive SecurityContextHolder boilerplate code across services. Centralizes user context access pattern.
 * INTEGRATION: Used by services (UserService, QuestionService, QuestionPaperService) to identify current user for ownership checks, authorization, and audit trails.
 * THREAD SAFETY: SecurityContextHolder uses ThreadLocal storage, making this utility thread-safe for request processing.
 * ERROR HANDLING: Implicit assumption that user is authenticated when called. Should be used only in authenticated contexts (after JWT filter).
 * PERFORMANCE: Lightweight utility with minimal overhead. SecurityContextHolder access is optimized in Spring Security.
 * DESIGN PATTERN: Follows utility class pattern with private constructor to prevent instantiation. Static method for global access.
 * ALTERNATIVES: Could use @AuthenticationPrincipal in controller methods, but this utility enables service-layer access without parameter passing.
 */
public class UserUtil {
    private UserUtil(){}

    public static UserPrincipal getUserAuthentication(){
        return (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}
