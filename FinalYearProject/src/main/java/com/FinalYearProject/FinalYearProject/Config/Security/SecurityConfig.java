package com.FinalYearProject.FinalYearProject.Config.Security;

import com.FinalYearProject.FinalYearProject.Config.Security.Filter.JwtFilter;
import com.FinalYearProject.FinalYearProject.Service.MyUserDetailsServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security Configuration for JWT-based Authentication
 * PURPOSE:
 * Main security configuration class that sets up JWT authentication, role-based authorization,
 * and stateless session management for a REST API.
 * SECURITY ARCHITECTURE:
 * 1. STATELESS SESSIONS: Uses JWT tokens instead of server-side sessions (SessionCreationPolicy.STATELESS)
 * 2. CSRF DISABLED: Not needed for stateless REST APIs (no browser session cookies)
 * 3. JWT FILTER: Custom JwtFilter processes tokens before Spring's default auth filter
 * 4. ROLE-BASED ACCESS: URL patterns protected by specific authorities (ROLE_ADMIN, ROLE_STUDENT, etc.)
 * KEY COMPONENTS:
 * - AuthenticationProvider: Uses DaoAuthenticationProvider with BCrypt password encoding
 * - PasswordEncoder: BCrypt with strength 12 (high security, slower hashing)
 * - AuthenticationManager: Bean for programmatic authentication
 * - SecurityFilterChain: Main security configuration with URL authorization rules
 * URL AUTHORIZATION RULES:
 * - /admin/** → Requires ROLE_ADMIN authority
 * - /student/** → Requires ROLE_STUDENT or ROLE_ADMIN
 * - /teacher/** → Requires ROLE_TEACHER or ROLE_ADMIN
 * - /supervisor → Requires ROLE_SUPERVISOR or ROLE_ADMIN
 * - Public paths: login, auth, static resources (CSS, JS, images) → Permit all
 * - All other requests → Require authentication
 * FILTER CHAIN ORDER:
 * 1. JwtFilter (custom) - processes JWT tokens
 * 2. UsernamePasswordAuthenticationFilter (Spring default) - processes form login
 * SECURITY NOTES:
 * - Uses app.version property for versioned API endpoints
 * - BCrypt strength 12 provides strong password hashing (recommended for production)
 * - Stateless design enables horizontal scaling (no session affinity needed)
 * - JWT filter validates tokens before Spring's default authentication
 */
@EnableMethodSecurity
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Value("${app.version}")
    private String appVersion;
    @Autowired
    private JwtFilter jwtFilter;
    @Autowired
    private MyUserDetailsServices myUserDetailsServices;

    // this Constructor is used to Customize the Security Filter Chain flow
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        return httpSecurity
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session ->
                          session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(
                        auth ->
                                auth
                                        //TODO remove ROLE_ADMIN from where it does not belong
                                        .requestMatchers(appVersion+"/admin/**").hasAuthority("ROLE_ADMIN")
                                        .requestMatchers(appVersion+"/student/**").hasAnyAuthority("ROLE_STUDENT","ROLE_ADMIN")
                                        .requestMatchers(appVersion+"/teacher/**").hasAnyAuthority("ROLE_TEACHER","ROLE_ADMIN")
                                        .requestMatchers(appVersion+"/supervisor/**").hasAnyAuthority("ROLE_SUPERVISOR","ROLE_ADMIN")
                                        .requestMatchers(
                                                // All of the below paths are permitted with put being authorised
                                                appVersion+"/login",
                                                appVersion+"/auth/**",
                                                "/login",
                                                "/register",
//                                                "/admin-dashboard",
//                                                "/teacher-dashboard",
//                                                "/supervisor-dashboard",
//                                                "/student-dashboard",
                                                "/css/**",
                                                "/js/**",
                                                "/images/**",
                                                "/webjars/**",
                                                "/"
                                        )
                                        .permitAll()
                                        .anyRequest()
                                        .authenticated()
                )
                // this calls the been authenticationProvider the nect function for custom logic
                .authenticationProvider(authenticationProvider())
                //this line adds the jwt custom logic class after the previous set of instruction are completed
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }
    //better practice to use this way
    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder(12);
    }
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setPasswordEncoder(passwordEncoder());
        provider.setUserDetailsService(myUserDetailsServices);
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }
}
