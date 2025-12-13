package com.FinalYearProject.FinalYearProject.Domain;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public class UserPrincipal implements UserDetails {


    private final User user;

    public UserPrincipal(User user){
        this.user=user;
    }

    @Override
    public boolean isEnabled() {
        return user.isIs_enable();
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
        System.out.println("DEBUG: User role from DB -> " + role);
        if (role == null || role.isBlank()) {
            throw new IllegalArgumentException("User role cannot be null or empty");
        }
        if (!role.startsWith("ROLE_")) {
            role = "ROLE_" + role.toUpperCase();
        }
        System.out.println("DEBUG: Final GrantedAuthority -> " + role);
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
}
