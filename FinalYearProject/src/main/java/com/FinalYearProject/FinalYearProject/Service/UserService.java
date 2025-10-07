package com.FinalYearProject.FinalYearProject.Service;

import com.FinalYearProject.FinalYearProject.Domain.Conformation;
import com.FinalYearProject.FinalYearProject.Repository.ConformationRepository;
import com.FinalYearProject.FinalYearProject.Repository.UserRepository;
import com.FinalYearProject.FinalYearProject.Domain.User;
import lombok.AllArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final ConformationRepository conformationRepository;
    private final ConformationService conformationService;

    public User findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public User saveUser(User user) {
        if (userRepository.existByEmail(user.getEmail())) {
            throw new RuntimeException("Email already taken");
        } else {
            user.setIs_enable(false);
            userRepository.save(user);

            Conformation conformation = new Conformation(user);
            conformationRepository.save(conformation);
            conformationService.sendEmail(user.getEmail(), user.getName(), conformation.getToken());
            return user;
        }
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username);

        if (user == null) {
            throw new UsernameNotFoundException("User not found with username: " + username);
        }

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getName())
                .password(user.getPassword())
                .roles(user.getRole())
                .build();
    }
}
