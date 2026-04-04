package com.FinalYearProject.FinalYearProject.Config.Security.Filter;

import com.FinalYearProject.FinalYearProject.Service.JwtService;
import com.FinalYearProject.FinalYearProject.Service.MyUserDetailsServices;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;


/**
 JWT Authentication Filter for Spring Security
 PURPOSE:
 Intercepts HTTP requests to validate JWT tokens and establish authentication context.
 Extends OncePerRequestFilter to ensure single execution per request.
 WORKFLOW:
 PUBLIC PATH CHECK: Skips authentication for login, register, static resources, etc.
 TOKEN EXTRACTION: Extracts "Bearer <token>" from Authorization header.
 TOKEN VALIDATION: Checks token expiration and signature validity.
 USER LOADING: Loads UserDetails if token is valid.
 AUTH SETUP: Creates UsernamePasswordAuthenticationToken and sets it in SecurityContext.
 KEY BEHAVIOR:
 Returns 401 UNAUTHORIZED for expired/invalid tokens
 Sets authentication in SecurityContextHolder for valid tokens
 Continues filter chain for public paths or after authentication setup
 Uses ApplicationContext.getBean() for fresh UserDetailsService instance
 SECURITY NOTES:
 Never validates tokens for public endpoints (performance/security)
 Validates both token expiration and signature
 Sets WebAuthenticationDetails with request info (IP, session)
 Follows Bearer token standard (RFC 6750)
 */
@Slf4j
@Component
public class JwtFilter extends OncePerRequestFilter {
    @Autowired
    private JwtService jwtService;
    @Autowired
    private ApplicationContext applicationContext;
    @Value("${app.version}")
    private String appVersion;
    String token = null;
    String username=null;
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String authHeader =request.getHeader("Authorization");//contain auth heder
        String path = request.getServletPath();
        if (//All allowed paths without auth
                path.startsWith(appVersion+"/login") ||
                        path.startsWith(appVersion+"/register") ||
                        path.startsWith(appVersion+"/auth")||
                        path.equals("/login") ||
                        path.equals("/register") ||
                        path.equals("/admin-dashboard")||
                        path.equals("/teacher-dashboard")||
                        path.equals("/supervisor-dashboard")||
                        path.equals("/student-dashboard")||
                        path.startsWith("/confirm") ||
                        path.equals("/favicon.ico") ||
                        path.startsWith("/css") ||
                        path.startsWith("/js") ||
                        path.startsWith("/images") ||
                        path.startsWith("/webjars") ||
                        path.equals("/")
        ) {
            System.out.println("Request:"+request.getRequestURI());
            filterChain.doFilter(request, response);
            return;
        }
        if(authHeader!=null && authHeader.startsWith("Bearer ")){
            token=authHeader.substring(7); //separate the heder type form heder
            username= jwtService.extractUserEmail(token);
            try {
                System.out.println("Request:"+request.getRequestURI());
                jwtService.isTokenExpiredOrThrow(token);
            } catch (Exception e) {
                ObjectMapper objectMapper=new ObjectMapper();
                Map<String,String> error=new HashMap<>();

                error.put("message","Token Expired — Please Login Again or "+e.getMessage());
                error.put("path",request.getRequestURI());
                error.put("timestamp", LocalDateTime.now().toString());
                error.put("status",String.valueOf(HttpStatus.UNAUTHORIZED.value()));

                response.setStatus(HttpStatus.UNAUTHORIZED.value());
                response.getWriter().write(objectMapper.writeValueAsString(error));

                logger.warn("Token Expired — Please Login Again or "+e.getMessage(),e);
                return;
            }
        }
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null){
            UserDetails userDetails =applicationContext.getBean(MyUserDetailsServices.class).loadUserByUsername(username);
            if(jwtService.validateToken(token , userDetails)){
                UsernamePasswordAuthenticationToken authenticationToken =
                        new UsernamePasswordAuthenticationToken
                                (userDetails,null,userDetails.getAuthorities());
                authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            }
        }
        filterChain.doFilter(request,response);
    }
    @PostConstruct
    public void init(){ // no use just for debugging
        System.out.println("✅ JwtFilter initialized and added to SecurityFilterChain");
    }
}
