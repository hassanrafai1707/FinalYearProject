package com.FinalYearProject.FinalYearProject.Service;

import com.FinalYearProject.FinalYearProject.Domain.Conformation;
import com.FinalYearProject.FinalYearProject.Domain.User;
import com.FinalYearProject.FinalYearProject.Domain.UserPrincipal;
import com.FinalYearProject.FinalYearProject.Exceptions.UserEeceptions.*;
import com.FinalYearProject.FinalYearProject.Repository.UserRepository;
import com.FinalYearProject.FinalYearProject.Util.UserUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
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
public class UserService {
    @Autowired
    private  UserRepository userRepository;
    @Autowired
    private ConformationService conformationService;
    @Autowired
    private AuthenticationManager authManager;
    @Autowired
    private JwtService jwtService;
    @Autowired
    private RedisService redisService;
    @Autowired
    private BCryptPasswordEncoder encoder;

    //TODO send user dto and not user
    //  CREATE user
    @Transactional
    public User creatUser(User user) {
        if (existsByEmail(user.getEmail())) {
            throw new DuplicateEmailException( "Email already taken");
        }

        user.setIs_enable(false);
        user.setRole("ROLE_STUDENT");
        user.setPassword(encoder.encode(user.getPassword()));
        user.setExpired(true);
        user.setLocked(true);

        Conformation conformation = new Conformation(user);

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
        if(log.isDebugEnabled()){
            System.out.println(redisService.get(conformation.getToken(),Conformation.class));
            log.debug(redisService.get(conformation.getToken(),Conformation.class).toString());
            System.out.println("user saved success");
        }
        userRepository.save(user);
        return user;
    }

    //  READ (all users)
    public List<User> findAllUsers() {
        return userRepository
                .findAll()
                .stream()
                .sorted(
                        Comparator.comparing(
                                User::getId
                        )
                ).toList();
    }

    public Page<User> findAllUsersPage(int pageNo,int size){
        Pageable pageable= PageRequest.of(pageNo,size);
        Page<User> temp=userRepository.findAll(pageable);
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
                .orElseThrow(() ->
                        new UsernameNotFoundException(
                                "User not found with id: " + id
                        )
                );
    }

    //  READ (by email)
    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "User not found with email"
                        +email
                        )
                );
    }

    //  UPDATE

    public User updateUserEmail(String NewEmail) {
        String email=UserUtil.getUserAuthentication().getUsername();
        if(email.isEmpty()){
            throw new RuntimeException("Some thing went wrong on user in security context holder ");
        }
        User existingUser = findByEmail(email);
        existingUser.setEmail(NewEmail);
        return userRepository.save(existingUser);
    }

    public User updateUserPassword(String newPassword){
        String email= UserUtil.getUserAuthentication().getUsername();
        if(email.isEmpty()){
            throw new RuntimeException("Some thing went wrong on user in security context holder ");
        }
        User existingUser=findByEmail(email);
        existingUser.setPassword(encoder.encode(newPassword));
        return userRepository.save(existingUser);
    }

    public User updateUserPasswordByEmail(String email,String password,String adminPassword){
        UserPrincipal adminUser=UserUtil.getUserAuthentication();
        if (adminUser==null){
            throw new RuntimeException("some thing went wrong user not fount in security context");
        }
        if (!(adminUser.getRole().contains("ROLE_ADMIN"))){
            throw new UserNotAuthorizesException("User not Authorized to make this request");
        }
        if (!(encoder.matches(adminPassword,adminUser.getPassword()))){
            throw new WrongPasswordException("You gave the wrong password");
        }
        User user=findByEmail(email);
        user.setPassword(encoder.encode(password));
        userRepository.save(user);
        return user;
    }

    public User updateUserPasswordById(Long id,String password,String adminPassword){
        UserPrincipal adminUser=UserUtil.getUserAuthentication();
        if (adminUser==null){
            throw new RuntimeException("some thing went wrong user not fount in security context");
        }
        if (!(adminUser.getRole().contains("ROLE_ADMIN"))){
            throw new UserNotAuthorizesException("User not Authorized to make this request");
        }
        if (!(encoder.matches(adminPassword,adminUser.getPassword()))){
            throw new WrongPasswordException("You gave the wrong password");
        }
        User user=findUserById(id);
        user.setPassword(encoder.encode(password));
        userRepository.save(user);
        return user;
    }

    public User updateUserRoleById(String role,Long id,String password){
        UserPrincipal userPrincipal=UserUtil.getUserAuthentication();
        if (userPrincipal==null){
            throw new RuntimeException("some thing went wrong user not fount in security context");
        }
        if (!(userPrincipal.getRole().contains("ROLE_ADMIN"))){
            throw new UserNotAuthorizesException("User not Authorized to make this request");
        }
        if (!(encoder.matches(password,userPrincipal.getPassword()))){
            throw new UserNotAuthorizesException("User not Authorized to make this request due to password");
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

    private User updateRole(User user,String role){
        user.setRole(role);
        return userRepository.save(user);
    }


    public User updateUserRoleByEmail(String email,String role,String password){
        UserPrincipal userPrincipal=UserUtil.getUserAuthentication();
        if (userPrincipal==null){
            throw new RuntimeException("some thing went wrong user not fount in security context");
        }
        if (!(userPrincipal.getRole().contains("ROLE_ADMIN"))){
            throw new UserNotAuthorizesException("User not Authorized to make this request");
        }
        if (!(encoder.matches(password,userPrincipal.getPassword()))){
            throw new UserNotAuthorizesException("User not Authorized to make this request due to password");
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
    public void deleteUserByEmail(String email,String adminPassword) {
        UserPrincipal adminUser=UserUtil.getUserAuthentication();
        if (adminUser==null){
            throw new RuntimeException("some thing went wrong user not fount in security context");
        }
        if (!(adminUser.getRole().contains("ROLE_ADMIN"))){
             throw new UserNotAuthorizesException("User not authorized to make this request");
        }
        if (!(encoder.matches(adminPassword,adminUser.getPassword()))){
            throw new WrongPasswordException("wrong password");
        }
        else {
            userRepository.deleteByEmail(email);
        }
    }

    //  DELETE (by ID)
    @Transactional
    public void deleteUserById(Long id , String adminPassword) {
        UserPrincipal adminUser = UserUtil.getUserAuthentication();
        if (adminUser==null){
            throw new UserNotAuthorizesException("some thing went wrong user not fount in security context");
        }
        if (!(adminUser.getRole().equals("ROLE_ADMIN"))){
            throw new UserNotAuthorizesException("User not authorized to make this request");
        }
        if (!(encoder.matches(adminPassword, adminUser.getPassword()))){
            throw new WrongPasswordException("wrong password try again");
        }
        else {
            userRepository.deleteById(id);
        }
    }

    @Transactional
    public void deleteUserInBatchById(List<Long> ids , String adminPassword) {
        UserPrincipal adminUser= UserUtil.getUserAuthentication();
        if (adminUser==null){
            throw new RuntimeException("some thing went wrong user not fount in security context");
        }
        if (!(adminUser.getRole().contains("ROLE_ADMIN"))){
            throw new UserNotAuthorizesException("User not authorized to make this request");
        }
        if (!(encoder.matches(adminPassword,adminUser.getPassword()))){
            throw new WrongPasswordException("wrong password");
        }
        else {
            userRepository.deleteUserInBatchById(ids);
        }
    }

    @Transactional
    public void deleteUserInBatchEmail(List<String> emails , String adminPassword){
        UserPrincipal adminUser= UserUtil.getUserAuthentication();
        if (adminUser==null){
            throw new RuntimeException("some thing went wrong user not fount in security context");
        }
        if (!(adminUser.getRole().contains("ROLE_ADMIN"))){
            throw new UserNotAuthorizesException("User not authorized to make this request");
        }
        if (!(encoder.matches(adminPassword,adminUser.getPassword()))){
            throw new WrongPasswordException("wrong password");
        }
        else {
            userRepository.deleteUserInBatchByEmail(emails);
        }
    }

    //  Suspend User
    public User suspendUserById(Long id , String adminPassword) {
        UserPrincipal adminUser= UserUtil.getUserAuthentication();
        if (adminUser==null){
            throw new RuntimeException("some thing went wrong user not fount in security context");
        }
        if (!(adminUser.getRole().contains("ROLE_ADMIN"))){
            throw new UserNotAuthorizesException("User not authorized to make this request");
        }
        if (!(encoder.matches(adminPassword,adminUser.getPassword()))){
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

    public User unsuspendUserById(Long id,String adminPassword){
        UserPrincipal adminUser= UserUtil.getUserAuthentication();
        if (adminUser==null){
            throw new RuntimeException("some thing went wrong user not fount in security context");
        }
        if (!(adminUser.getRole().contains("ROLE_ADMIN"))){
            throw new UserNotAuthorizesException("User not authorized to make this request");
        }
        if (!(encoder.matches(adminPassword,adminUser.getPassword()))){
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

    public User suspendUserByEmail(String email, String adminPassword){
        UserPrincipal adminUser= UserUtil.getUserAuthentication();
        if (adminUser==null){
            throw new RuntimeException("some thing went wrong user not fount in security context");
        }
        if (!(adminUser.getRole().contains("ROLE_ADMIN"))){
            throw new UserNotAuthorizesException("User not authorized to make this request");
        }
        if (!(encoder.matches(adminPassword,adminUser.getPassword()))){
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

    public User unsuspendUserByEmail(String email,String adminPassword){
        UserPrincipal adminUser= UserUtil.getUserAuthentication();
        if (adminUser==null){
            throw new RuntimeException("some thing went wrong user not fount in security context");
        }
        if (!(adminUser.getRole().contains("ROLE_ADMIN"))){
            throw new UserNotAuthorizesException("User not authorized to make this request");
        }
        if (!(encoder.matches(adminPassword,adminUser.getPassword()))){
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
        Authentication authentication = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, password)
        );

        if (authentication.isAuthenticated()) {
            UserPrincipal foundUser=(UserPrincipal) authentication.getPrincipal();
            if (!foundUser.isEnabled() || !foundUser.isAccountNonExpired() || !foundUser.isAccountNonExpired() ){
                throw new UserLockedException("Login failed due to user is locked");
            }
            else {
                return jwtService.jwtToken(email, foundUser.getRole());
            }
        }

        throw new RuntimeException("Login failed");
    }

    public String verifyLoginById(Long id,String password){
        User user=findUserById(id);
        Authentication authentication=authManager.authenticate(
                new UsernamePasswordAuthenticationToken(user.getEmail(),password)
        );

        if (authentication.isAuthenticated()){
            UserPrincipal userPrincipal=(UserPrincipal) authentication.getPrincipal();
            if (
                    userPrincipal==null
                    ||!userPrincipal.isAccountNonExpired()
                    ||!userPrincipal.isAccountNonLocked()
                    ||!userPrincipal.isEnabled()
            ){
                throw new UserLockedException("Login failed due to user is locked");
            }
            return jwtService.jwtToken(userPrincipal.getUsername(),userPrincipal.getRole());
        }
        throw new RuntimeException("Login failed");
    }

    //todo use frontend to logout user

    // VERIFY Token
    @Transactional(propagation = Propagation.REQUIRED)
    public Boolean verifyTokenAndOTP(String email ,String token, int otp) {
        try {
            Conformation conformation=redisService.get(email,Conformation.class);
            if ( conformation !=null &&
                    otp == conformation.getOtp() &&
                    existsByEmail(conformation.getUser().getEmail())
                    && token.equals(conformation.getToken())
            ) {
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

    public List<User> listOfUserByRole(String role){
        return userRepository.findByRole(role);
    }
    public Page<User> listOfUserByRole(String role ,int pageNo, int size){
        Pageable pageable=PageRequest.of(pageNo,size);
        return userRepository.findByRole(role,pageable);
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

    public void regenerateOtp(String email){
        try{
            Conformation existingConformation=redisService.get(email,Conformation.class);
            existingConformation.generateOtp();
            redisService.set(email,existingConformation,10L);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
