package com.FinalYearProject.FinalYearProject.Service;

import com.FinalYearProject.FinalYearProject.Domain.Conformation;
import com.FinalYearProject.FinalYearProject.Domain.User;
import com.FinalYearProject.FinalYearProject.Repository.ConformationRepository;
import com.FinalYearProject.FinalYearProject.Repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final ConformationRepository conformationRepository;
    private final ConformationService conformationService;
    private final AuthenticationManager authManager;
    private final JwtService jwtService;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);

    //  CREATE user
    public User saveUser(User user) {
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("Email already taken");
        }

        user.setIs_enable(false);
        user.setPassword(encoder.encode(user.getPassword()));
        user.setExpired(false);
        user.setLocked(false);

        userRepository.save(user);

        Conformation conformation = new Conformation(user);
        conformationRepository.save(conformation);
        conformationService.sendEmail(user.getEmail(), user.getName(), conformation.getToken());

        return user;
    }

    //  READ (all users)
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    //  READ (by ID)
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
    }

    //  READ (by email)
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    //  READ (by username)
    public User findByUsername(String name) {
        return userRepository.findByName(name)
                .orElseThrow(() -> new RuntimeException("User not found: " + name));
    }

    //  UPDATE
    public User updateUser(Long id, User updatedUser) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));

        existingUser.setName(updatedUser.getName());
        existingUser.setEmail(updatedUser.getEmail());

        if (updatedUser.getPassword() != null && !updatedUser.getPassword().isEmpty()) {
            existingUser.setPassword(encoder.encode(updatedUser.getPassword()));
        }

        existingUser.setLocked(updatedUser.isLocked());
        existingUser.setExpired(updatedUser.isExpired());
        existingUser.setIs_enable(updatedUser.isIs_enable());

        return userRepository.save(existingUser);
    }

    //  DELETE (by user)
    public void deleteUser(User user) {
        if (userRepository.existsByEmail(user.getEmail())) {
            userRepository.deleteById(user.getId());
            conformationRepository.deleteByUser(user);
        } else {
            throw new RuntimeException("User not found with email: " + user.getEmail());
        }
    }

    //  DELETE (by ID)
    public void deleteUserById(Long id) {
        if (userRepository.existsById(id)) {
            userRepository.deleteById(id);
        } else {
            throw new RuntimeException("User not found with id: " + id);
        }
    }

    //  DELETE ALL
    public void deleteAllUsers() {
        userRepository.deleteAll();
        conformationRepository.deleteAll();
    }

    //  Suspend User
    public void suspendUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
        user.setLocked(true);
        user.setExpired(true);
        userRepository.save(user);
    }

    //  VERIFY LOGIN
    public String verifyLogin(User user) {
        Authentication authentication = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(user.getEmail(), user.getPassword())
        );
        if (authentication.isAuthenticated()){
            return jwtService.jwtToken(user.getEmail());
        }
        else {
            throw new RuntimeException("Invalid credentials");
        }
    }
}
