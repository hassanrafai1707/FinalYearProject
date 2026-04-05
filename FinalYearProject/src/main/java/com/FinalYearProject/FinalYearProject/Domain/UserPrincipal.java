package com.FinalYearProject.FinalYearProject.Domain;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * UserPrincipal Class - Spring Security UserDetails Implementation
 * PURPOSE: Adapter class that bridges custom User domain entity with Spring Security's UserDetails interface. Enables Spring Security integration with custom user storage.
 * SECURITY INTEGRATION: Implements all UserDetails contract methods required by Spring Security for authentication and authorization workflows.
 * AUTHORITY MANAGEMENT: getAuthorities() converts User.role field to Spring Security GrantedAuthority with "ROLE_" prefix validation. Includes debug logging for role transformation.
 * ACCOUNT STATUS MAPPING: Maps User entity status fields to Spring Security status methods: isEnabled() -> user.is_enable(), isAccountNonLocked() -> !user.isLocked(), etc.
 * CREDENTIAL MANAGEMENT: getUsername() returns email (Spring Security username equivalent). getPassword() returns hashed password for credential validation.
 * ROLE PROCESSING LOGIC: Validates role existence, ensures "ROLE_" prefix, converts to uppercase. Throws IllegalArgumentException for null/empty roles to prevent security misconfiguration.
 * DEBUG SUPPORT: Includes System.out.println debugging for role transformation during development. Helps troubleshoot role-based access control issues.
 * SECURITY CONTRACT COMPLIANCE: Fully implements UserDetails interface enabling seamless integration with DaoAuthenticationProvider, method security, and authorization decisions.
 * IMMUTABILITY: User reference is final - cannot be modified after construction. Ensures consistent security context during request processing.
 * ID EXPOSURE: Provides getId() method for easy access to user's database ID in security expressions and business logic.
 * INTEGRATION: Used by MyUserDetailsServices to load user details. Referenced in JWT token creation and validation. Supports @PreAuthorize method security annotations.
 */
public class UserPrincipal implements UserDetails {


    private final User user;

    public UserPrincipal(User user){
        this.user=user;
    }

    @Override
    public boolean isEnabled() {
        return user.is_enable();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return !user.isExpired();
    }

    @Override
    public boolean isAccountNonLocked() {
        return !user.isLocked();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        String role = user.getRole();
        if (role == null || role.isBlank()) {
            throw new IllegalArgumentException("User role cannot be null or empty");
        }
        if (!role.startsWith("ROLE_")) {
            role = "ROLE_" + role.toUpperCase();
        }
        return List.of(new SimpleGrantedAuthority(role));
    }

    @Override
    public boolean isAccountNonExpired() {
        return !user.isExpired();
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getEmail();
    }

    public Long getId(){
        return user.getId();
    }

    public String getRole(){
        return user.getRole();
    }

    public User getUser(){
        return user;
    }
}
