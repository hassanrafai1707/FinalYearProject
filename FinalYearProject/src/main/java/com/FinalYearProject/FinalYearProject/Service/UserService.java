package com.FinalYearProject.FinalYearProject.Service;

import com.FinalYearProject.FinalYearProject.Domain.Conformation;
import com.FinalYearProject.FinalYearProject.Domain.User;
import com.FinalYearProject.FinalYearProject.Domain.UserPrincipal;
import com.FinalYearProject.FinalYearProject.Exceptions.UserEeceptions.*;
import com.FinalYearProject.FinalYearProject.Repository.UserRepository;
import com.FinalYearProject.FinalYearProject.Util.UserUtil;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * UserService - Core Business Logic Service for User Management and Authentication
 * PURPOSE: Comprehensive service handling all user-related operations including registration, authentication, account management, admin functions, and email verification workflows.
 * USER LIFECYCLE MANAGEMENT: Complete CRUD operations for users with role-based authorization. Supports creation, retrieval, updates, deletion, suspension, and role changes.
 * AUTHENTICATION & AUTHORIZATION: verifyLoginByEmail handles password authentication with Spring Security AuthenticationManager. JWT token generation upon successful login.
 * EMAIL VERIFICATION WORKFLOW: Registration creates user with disabled account. Sends verification email with token and OTP via Redis cache. verifyTokenAndOTP validates and activates account.
 * ADMIN OPERATIONS: Extensive admin functionality requiring admin password re-authentication for sensitive operations (delete, suspend, role changes, password resets). Implements defense-in-depth security.
 * SECURITY IMPLEMENTATION: BCrypt password hashing with strength 12. Account lockout and expiration flags. Admin password verification beyond JWT role validation.
 * REDIS INTEGRATION: Stores email verification data (Conformation objects) with 10-minute TTL. Supports OTP regeneration and verification.
 * TRANSACTION MANAGEMENT: @Transactional on write operations ensures data consistency. Propagation.REQUIRED for verification workflow.
 * BATCH OPERATIONS: deleteUserInBatchById/Email for efficient bulk user management. Pagination support for user listings.
 * ERROR HANDLING: Comprehensive exception hierarchy - UserNotFoundException, UserNotAuthorizesException, WrongPasswordException, DuplicateEmailException, UserLockedException.
 * INTEGRATION: Works with UserRepository (data), JwtService (tokens), ConformationService (emails), RedisService (cache), BCryptPasswordEncoder (security), AuthenticationManager (auth).
 */
@Slf4j
@Service
public class UserService {
    private final UserRepository userRepository;
    private final ConformationService conformationService;
    private final AuthenticationManager authManager;
    private final JwtService jwtService;
    private final RedisService redisService;
    private final BCryptPasswordEncoder encoder;

    public UserService(
            UserRepository userRepository,
            ConformationService conformationService,
            AuthenticationManager authManager,
            JwtService jwtService,
            RedisService redisService,
            BCryptPasswordEncoder encoder
    ){
        this.userRepository=userRepository;
        this.conformationService=conformationService;
        this.authManager=authManager;
        this.jwtService=jwtService;
        this.redisService=redisService;
        this.encoder=encoder;
    }

    //  CREATE user
    @Transactional
    public User creatUser(@NonNull User user) {
        if (existsByEmail(user.getEmail())) {
            throw new DuplicateEmailException("Email already taken");
        }
        user.set_enable(false);
        user.setRole("ROLE_STUDENT");
        user.setPassword(encoder.encode(user.getPassword()));
        user.setExpired(true);
        user.setLocked(true);
        Conformation conformation = new Conformation(user);
        User saveUser=userRepository.save(user);
        redisService.set(
                conformation.getUser().getEmail(),
                conformation,
                10L
        );
        conformationService.sendEmail(
                user.getEmail(),
                user.getName(),
                conformation.getToken(),
                conformation.getOtp()
        );
        return saveUser;
    }

    //  READ (all users)
    public List<User> findAllUsers() {
        return userRepository.findAll();
    }

    public Page<User> findAllUsersPage(int pageNo,int size){
        Page<User> temp=userRepository.findAll(PageRequest.of(pageNo,size));
        if (!(temp.isEmpty())){
            return temp;
        }
        else {
            throw new UsernameNotFoundException("no more users in db");
        }
    }

    //  READ (by ID)
    public User findUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + id));
    }

    //  READ (by email)
    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email" +email));
    }

    //  UPDATE

    public User updateUserEmail(String NewEmail) {
        User existingUser = findByEmail(UserUtil.getUserAuthentication().getUsername());
        existingUser.setEmail(NewEmail);
        existingUser.set_enable(false);
        existingUser.setPassword(encoder.encode(existingUser.getPassword()));
        existingUser.setExpired(true);
        existingUser.setLocked(true);
        Conformation conformation = new Conformation(existingUser);
        redisService.set(
                conformation.getUser().getEmail(),
                conformation,
                10L
        );
        conformationService.sendEmail(
                existingUser.getEmail(),
                existingUser.getName(),
                conformation.getToken(),
                conformation.getOtp()
        );
        return userRepository.save(existingUser);
    }

    public User updateUserPassword(String newPassword){
        User existingUser=findByEmail(UserUtil.getUserAuthentication().getUsername());
        existingUser.setPassword(encoder.encode(newPassword));
        return userRepository.save(existingUser);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public User updateUserPasswordByEmail(String email,String password,String adminPassword){
        if (!(matchPasswords(adminPassword,UserUtil.getUserAuthentication().getPassword()))){
            throw new WrongPasswordException("You gave the wrong password");
        }
        User user=findByEmail(email);
        user.setPassword(encoder.encode(password));
        return userRepository.save(user);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public User updateUserPasswordById(Long id,String password,String adminPassword){
        if (!(matchPasswords(adminPassword,UserUtil.getUserAuthentication().getPassword()))){
            throw new WrongPasswordException("You gave the wrong password");
        }
        User user=findUserById(id);
        user.setPassword(encoder.encode(password));
        return userRepository.save(user);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public User updateUserRoleById(String role,Long id,String password){
        if (!(matchPasswords(password,UserUtil.getUserAuthentication().getPassword()))){
            throw new WrongPasswordException("You gave the wrong password");
        }
        switch (role){
            case "ROLE_ADMIN"->{
                User user =findUserById(id);
                return updateRole(user,role);
            }
            case "ROLE_TEACHER"->{
                User user =findUserById(id);
                return updateRole(user,role);
            }
            case "ROLE_STUDENT"->{
                User user =findUserById(id);
                return updateRole(user,role);
            }
            case "ROLE_SUPERVISOR"->{
                User user =findUserById(id);
                return updateRole(user,role);
            }
            default -> throw new RoleNotValidException("the role not formated properly");
        }
    }

    public Boolean matchPasswords(String rawPassword,String encodedPassword){
        return encoder.matches(rawPassword,encodedPassword);
    }

    private User updateRole(User user,String role){
        user.setRole(role);
        return userRepository.save(user);
    }


    @PreAuthorize("hasRole('ADMIN')")
    public User updateUserRoleByEmail(String email,String role,String password){
        if (!(matchPasswords(password,UserUtil.getUserAuthentication().getPassword()))){
            throw new WrongPasswordException("You gave the wrong password");
        }
        switch (role){
            case "ROLE_ADMIN"->{
                User user =findByEmail(email);
                return updateRole(user,role);
            }
            case "ROLE_TEACHER"->{
                User user =findByEmail(email);
                return updateRole(user,role);
            }
            case "ROLE_STUDENT"->{
                User user =findByEmail(email);
                return updateRole(user,role);
            }
            case "ROLE_SUPERVISOR"->{
                User user =findByEmail(email);
                return updateRole(user,role);
            }
            default -> throw new RoleNotValidException("the role not formated properly");
        }
    }

    //  DELETE (by email)
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteUserByEmail(String email,String adminPassword) {
        if (!(matchPasswords(adminPassword,UserUtil.getUserAuthentication().getPassword()))){
            throw new WrongPasswordException("wrong password");
        }
        else {
            userRepository.deleteByEmail(email);
        }
    }

    //  DELETE (by ID)
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteUserById(Long id , String adminPassword) {
        if (!(matchPasswords(adminPassword, UserUtil.getUserAuthentication().getPassword()))){
            throw new WrongPasswordException("wrong password try again");
        }
        else {
            userRepository.deleteById(id);
        }
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteUserInBatchById(List<Long> ids , String adminPassword) {
        if (!(matchPasswords(adminPassword,UserUtil.getUserAuthentication().getPassword()))){
            throw new WrongPasswordException("wrong password");
        }
        else {
            userRepository.deleteUserInBatchById(validIDs(ids));
        }
    }

    public List<Long> validIDs(List<Long> ids){
        return userRepository.validIDs(ids.stream().distinct().toList());
    }

    public List<String> validEmails(List<String> emails){
        return userRepository.validEmails(emails.stream().distinct().toList());
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteUserInBatchEmail(List<String> emails , String adminPassword){
        if (!(matchPasswords(adminPassword,UserUtil.getUserAuthentication().getPassword()))){
            throw new WrongPasswordException("wrong password");
        }
        else {
            userRepository.deleteUserInBatchByEmail(validEmails(emails));
        }
    }

    //  Suspend User
    @PreAuthorize("hasRole('ADMIN')")
    public User suspendUserById(Long id , String adminPassword) {
        if (!(matchPasswords(adminPassword,UserUtil.getUserAuthentication().getPassword()))){
            throw new WrongPasswordException("wrong password");
        }
        else {
            User user = findUserById(id);
            user.setLocked(true);
            user.setExpired(true);
            userRepository.save(user);
            return user;
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    public User unsuspendUserById(Long id,String adminPassword){
        if (!(matchPasswords(adminPassword,UserUtil.getUserAuthentication().getPassword()))){
            throw new WrongPasswordException("wrong password");
        }
        else {
            User user = findUserById(id);
            user.setExpired(false);
            user.setLocked(false);
            userRepository.save(user);
            return user;
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    public User suspendUserByEmail(String email, String adminPassword){
        if (!(matchPasswords(adminPassword,UserUtil.getUserAuthentication().getPassword()))){
            throw new WrongPasswordException("wrong password");
        }
        else {
            User user=findByEmail(email);
            user.setExpired(true);
            user.setLocked(true);
            userRepository.save(user);
            return user;
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    public User unsuspendUserByEmail(String email,String adminPassword){
        if (!(encoder.matches(adminPassword,UserUtil.getUserAuthentication().getPassword()))){
            throw new WrongPasswordException("wrong password");
        }
        else {
            User user = findByEmail(email);
            user.setExpired(false);
            user.setLocked(false);
            userRepository.save(user);
            return user;
        }
    }

    //  VERIFY LOGIN
    public String verifyLoginByEmail(String email, String password) {
        User user1=findByEmail(email);
        user1.setLastLogin(LocalDateTime.now());
        user1.setExpired(false);
        userRepository.save(user1);
        Authentication authentication = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, password)
        );

        if (authentication.isAuthenticated()) {
            @NonNull UserPrincipal foundUser=(UserPrincipal) authentication.getPrincipal();
            if (!foundUser.isEnabled() || !foundUser.isAccountNonLocked() ){
                throw new UserLockedException("Login failed due to user is locked");
            }
            else {
                return jwtService.jwtToken(email, foundUser.getRole());
            }
        }

        throw new RuntimeException("Login failed");
    }

    public String verifyLoginById(Long id,String password){
        User user1=findUserById(id);
        user1.setLastLogin(LocalDateTime.now());
        user1.setExpired(false);
        userRepository.save(user1);
        Authentication authentication=authManager.authenticate(
                new UsernamePasswordAuthenticationToken(userRepository.userEmailById(id),password)
        );

        if (authentication.isAuthenticated()){
            @NonNull UserPrincipal userPrincipal=(UserPrincipal) authentication.getPrincipal();
            if (!userPrincipal.isAccountNonLocked() ||!userPrincipal.isEnabled()){
                throw new UserLockedException("Login failed due to user is locked");
            }
            return jwtService.jwtToken(userPrincipal.getUsername(),userPrincipal.getRole());
        }
        throw new RuntimeException("Login failed");
    }

    public void logout(){
        User user=UserUtil.getUserAuthentication().getUser();
        user.setExpired(true);
        userRepository.save(user);
    }

    // VERIFY Token
    public Boolean verifyTokenAndOTP(String email ,String token, int otp) {
        try {
            @NonNull Conformation conformation=redisService.get(email,Conformation.class);
            if (otp == conformation.getOtp() && existsByEmail(conformation.getUser().getEmail()) && token.equals(conformation.getToken())) {
                userRepository.updateIsEnableLockedExpiredToTrue(conformation.getUser().getEmail());
                redisService.delete(email);//delete conformation after verifying token and otp
                return Boolean.TRUE;
            }
            else {
                return Boolean.FALSE;
            }
        }
        catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    public List<User> listOfUserByRole(String role){
        return userRepository.findByRole(role);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public Page<User> listOfUserByRole(String role ,int pageNo, int size){
        return userRepository.findByRole(role,PageRequest.of(pageNo,size));
    }

    public Boolean existsByEmail(String email){
        if (userRepository.existsByEmail(email)){
            return Boolean.TRUE;
        }
        else {
            return Boolean.FALSE;
        }
    }

    public Boolean existsById(Long Id){
        if (userRepository.existsById(Id)){
            return Boolean.TRUE;
        }
        else {
            return Boolean.FALSE;
        }
    }
    //todo use this
    public User regenerateOtp(String email){
        User tempUser=findByEmail(email);
        if (
                tempUser.is_enable()||!tempUser.isExpired()||!tempUser.isLocked()
        ){
            return tempUser;
        }
        tempUser.setRole("ROLE_STUDENT");
        tempUser.set_enable(false);
        tempUser.setExpired(true);
        tempUser.setLocked(true);
        Conformation conformation = new Conformation(tempUser);
        redisService.set(
                conformation.getUser().getEmail(),
                conformation,
                10L
        );
        conformationService.sendEmail(
                tempUser.getEmail(),
                tempUser.getName(),
                conformation.getToken(),
                conformation.getOtp()
        );
        return userRepository.save(tempUser);
    }
}