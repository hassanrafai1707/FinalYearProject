package com.FinalYearProject.FinalYearProject.Service;

import com.FinalYearProject.FinalYearProject.Domain.User;
import com.FinalYearProject.FinalYearProject.Domain.UserPrincipal;
import com.FinalYearProject.FinalYearProject.Repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
/*this class is used to user by username but in this case i use it to load user by email
 used because this app does not allow more than one user with the ame gmail
*/
public class MyUserDetailsServices implements UserDetailsService {
    private final UserRepository userRepository;
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user =
                userRepository.findByEmail(email)
                        .orElseThrow(()->
                                new UsernameNotFoundException("User not found: " + email));
        return new UserPrincipal(user);
    }
}
