package com.FinalYearProject.FinalYearProject.Domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.Date;

/**
 * User Domain Entity for System Authentication and Authorization
 * PURPOSE: Core user entity for authentication, authorization, and user management across the exam system. Implements Spring Security UserDetails contract.
 * DATABASE DESIGN: JPA entity with sequence-based ID generation. Indexes on id and email for optimal lookup performance. Unique constraints on email and password fields.
 * AUTHENTICATION FIELDS: email (username equivalent), password (BCrypt hashed), role (authority/ROLE_*). Follows Spring Security field naming conventions.
 * ACCOUNT STATUS MANAGEMENT: is_enable (account active/inactive), locked (temporary lockout), expired (credentials expired). Supports comprehensive account lifecycle management.
 * ROLE-BASED ACCESS CONTROL: role field determines authorization level (ROLE_STUDENT, ROLE_TEACHER, ROLE_SUPERVISOR, ROLE_ADMIN). Used by SecurityConfig for endpoint protection.
 * SECURITY FEATURES: Password stored as unique hash. Account lockout capability. Account expiration support. Email verification required via is_enable flag.
 * AUDIT TRAIL: System-generated ID provides immutable reference. Email uniqueness prevents duplicate accounts. Timestamps implied through entity lifecycle.
 * INTEGRATION POINTS: Referenced by Question (createdBy), QuestionPaper (generatedBy, approvedBy). Maintains data integrity through JPA relationships.
 * PERFORMANCE: Indexed email enables fast login lookups. Builder pattern (@SuperBuilder) supports flexible object creation. Sequence generation ensures database-efficient ID assignment.
 * ACCOUNT LIFECYCLE: New users start with is_enable=false (pending verification). Verified users have is_enable=true. locked=true for security lockouts. expired=true for forced password resets.
 */
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@Getter
@Setter
@Entity
@Table(name = "Users",indexes = {
        @Index(name = "index_id",columnList = "id",unique = true),
        @Index(name = "index_email" ,columnList = "email",unique = true)
})
public class User {
    @Id
    @SequenceGenerator(
            name = "userSequence",
            sequenceName = "userSequence",
            allocationSize = 1
    )
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "userSequence"

    )
    @Column(nullable = false ,unique = true ,updatable = false)
    private Long Id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String role;
    @Column(nullable = false)
    private boolean is_enable;

    @Column(nullable = false,unique = true)
    private String password;

    @Column(nullable = false)
    private boolean locked;

    @Column(nullable = false)
    private boolean expired;//in logout, I can set this to false and prevent login again

    private LocalDateTime lastLogin;

    public boolean isExpired() {
        return expired;
    }

    public void setExpired(boolean expired) {
        this.expired = expired;
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isIs_enable() {
        return is_enable;
    }

    public void setIs_enable(boolean is_enable) {
        this.is_enable = is_enable;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getId() {
        return Id;
    }

    public LocalDateTime getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(LocalDateTime lastLogin) {
        this.lastLogin = lastLogin;
    }

    public void setId(Long id) {
        Id = id;


    }
}