package com.FinalYearProject.FinalYearProject.Repository;

import com.FinalYearProject.FinalYearProject.Domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * UserRepository - JPA Repository for User Data Access
 * PURPOSE: Data access layer for User entities providing CRUD operations and custom queries for user management, authentication, and administration.
 * REPOSITORY TYPE: Extends JpaRepository<User, Long> for standard CRUD with Long primary key. @Repository enables Spring Data JPA component scanning.
 * QUERY MIXTURE: Combines Spring Data derived queries (findByEmail, existsByEmail, findByRole) with custom @Query annotations for complex operations.
 * DERIVED QUERIES: findByEmail (authentication lookups), existsByEmail (duplicate checking), findByRole (role-based user listing). Spring generates queries from method names.
 * CUSTOM JPQL QUERIES: @Transactional @Modifying queries for update and delete operations. Includes batch deletions and account status updates.
 * BATCH OPERATIONS: deleteUserInBatchById and deleteUserInBatchByEmail support efficient bulk user deletion. Uses IN clause for performance.
 * ACCOUNT MANAGEMENT: updateIsEnableLockedExpiredToTrue resets account status flags (enable account, unlock, clear expiry). Used in email verification and admin account recovery.
 * TRANSACTION MANAGEMENT: @Transactional on modifying queries ensures atomic operations. @Modifying indicates data modification queries.
 * PERFORMANCE: Batch operations reduce database round-trips. ExistsByEmail provides efficient duplicate checking without loading full entity.
 * SECURITY INTEGRATION: findByEmail supports authentication lookups. ExistsByEmail prevents duplicate registrations. Role queries support admin user management.
 * INTEGRATION: Used by UserService for all user data operations. Supports authentication, registration, admin management, and account status workflows.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    //simple query handled by spring boot
    Optional<User> findByEmail(String email);
    Boolean existsByEmail(String email);
    List<User> findByRole(String role);
    Page<User> findByRole(String role, Pageable pageable);

    //complex query handle by @Query
    @Transactional
    @Modifying
    @Query("DELETE FROM User u WHERE u.email =:email")
    void deleteByEmail(@Param("email") String email);
    @Transactional
    @Modifying
    @Query("UPDATE User u set u.is_enable=true, u.locked=false,u.expired=false where u.email=:email")
    void updateIsEnableLockedExpiredToTrue(@Param("email")String email);
    @Transactional
    @Modifying
    @Query("DELETE User u WHERE u.Id IN:Ids")
    void deleteUserInBatchById(@Param("ids") List<Long> Ids);
    @Transactional
    @Modifying
    @Query("DELETE User u WHERE u.email IN:emails")
    void deleteUserInBatchByEmail(@Param("emails")List<String> emails);

    @Query("SELECT u.id FROM User u WHERE u.id IN:ids")
    List<Long> validIDs(@Param("ids") List<Long> ids);

    @Query("SELECT u.email FROM User u WHERE u.email IN:emails")
    List<String> validEmails(@Param("emails") List<String> emails);
}
