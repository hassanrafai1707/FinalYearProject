package com.FinalYearProject.FinalYearProject.Service;

import com.FinalYearProject.FinalYearProject.Domain.UserPrincipal;
import com.FinalYearProject.FinalYearProject.Repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;


/**
 * MyUserDetailsServices - Spring Security UserDetailsService Implementation
 * PURPOSE: Bridge between Spring Security authentication framework and custom User domain model. Loads user details by email for authentication and authorization.
 * USER LOOKUP STRATEGY: Uses email as username (Spring Security terminology) for user lookup. Implements UserDetailsService contract required by DaoAuthenticationProvider.
 * INTEGRATION: Converts User entity to UserPrincipal (implements UserDetails) for Spring Security compatibility. Provides authorities, credentials, and account status.
 * ERROR HANDLING: Throws UsernameNotFoundException when email not found. Exception translated to Spring Security authentication failure.
 * SECURITY CONTEXT: Loads user details during authentication process. Used by JwtFilter to validate tokens and load user authorities.
 * REPOSITORY INTEGRATION: Uses UserRepository.findByEmail for efficient email-based lookups. Returns Optional with exception on empty.
 * USER PRINCIPAL CREATION: Wraps User entity in UserPrincipal adapter that implements Spring Security interfaces. Maintains separation between domain and security.
 * SINGLE POINT OF TRUTH: Centralizes user loading logic for consistency across authentication methods (JWT, form login, etc.).
 * PERFORMANCE: Email lookups use indexed database column. UserPrincipal creation is lightweight.
 * EXTENSIBILITY: Can be extended to load additional user details, cache user data, or integrate with external authentication systems.
 */
@Service
@AllArgsConstructor
public class MyUserDetailsServices implements UserDetailsService {
    private final UserRepository userRepository;
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return new UserPrincipal(
                userRepository.findByEmail(email)
                .orElseThrow(
                        ()-> new UsernameNotFoundException("User not found: " + email)
                )
        );
    }
}
