package com.FinalYearProject.FinalYearProject.Service;

import com.FinalYearProject.FinalYearProject.Domain.Conformation;
import com.FinalYearProject.FinalYearProject.Repository.ConformationRepository;
import com.FinalYearProject.FinalYearProject.Repository.UserRepository;
import com.FinalYearProject.FinalYearProject.Domain.User;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService  {

    private final UserRepository userRepository;
    private final ConformationRepository conformationRepository;
    private final ConformationService conformationService;
    private  Conformation conformation;
    private BCryptPasswordEncoder encoder =new BCryptPasswordEncoder (12);
    private AuthenticationManager authManager;

    public UserService(UserRepository userRepository, ConformationRepository conformationRepository, ConformationService conformationService) {
        this.userRepository = userRepository;
        this.conformationRepository = conformationRepository;
        this.conformationService = conformationService;
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }
    public User findByUsername(String name){
        return userRepository.findByName(name).orElseThrow(()-> new RuntimeException("User not found: " + name));
    }

    public User saveUser(User user) {
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("Email already taken");
        }
        else {
            user.setIs_enable(false);
            user.setPassword(encoder.encode(user.getPassword()));
            userRepository.save(user);

            conformation = new Conformation(user);
            conformationRepository.save(conformation);
            conformationService.sendEmail(user.getEmail(), user.getName(), conformation.getToken());
            user.setExpired(false);
            user.setLocked(false);
            return user;
        }
    }
    //delete functions
    public void killUser(User user){
        if(userRepository.existsByEmail(user.getEmail())){
            userRepository.deleteById(user.getId());
            conformationRepository.deleteById(conformation.getId());
        }
        else {
            throw new RuntimeException("wrong email");
        }
    }
    public void killAllUser(User user){
        if(userRepository.existsByEmail(user.getEmail())){
            userRepository.deleteAll();
            conformationRepository.deleteAll();
        }
        else{
            throw new RuntimeException("wrong email");
        }
    }
    //Suspend user function
    public void SuspendUser(User user){
        if(userRepository.existsByEmail(user.getEmail())){
            user.setLocked(true);
            user.setExpired(true);
        }
        else {
            throw new RuntimeException("wrong email");
        }
    }
    public String verify(User user){
        Authentication authentication= authManager.authenticate(
                new UsernamePasswordAuthenticationToken(user.getName(),user.getPassword()));
        if(authentication.isAuthenticated()){
            return "h";
        }
        return "Fail";
    }
}
