package com.FinalYearProject.FinalYearProject.Service;

import com.FinalYearProject.FinalYearProject.Domain.Conformation;
import com.FinalYearProject.FinalYearProject.Domain.User;
import com.FinalYearProject.FinalYearProject.Exceptions.UserEeceptions.DuplicateEmailException;
import com.FinalYearProject.FinalYearProject.Exceptions.UserEeceptions.UserLockedException;
import com.FinalYearProject.FinalYearProject.Repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@AllArgsConstructor
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

    //  CREATE user
    public User saveUser(User user) {
        if (existsByEmail(user.getEmail())) {
            throw new DuplicateEmailException( "Email already taken");
        }

        user.setIs_enable(false);
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
        System.out.println(redisService.get(conformation.getToken(),Conformation.class));
        System.out.println("user saved success");

        userRepository.save(user);
        return user;
    }

    //  READ (all users)
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    //  READ (by ID)
    public User getUserById(Long id) {
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

    public User updateUserEmailById(Long id, String email) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + id));

        existingUser.setEmail(email);

        return userRepository.save(existingUser);
    }

    public User updateUserEmailByEmail (String oldEmail, String newEmail){

        User existingUser =userRepository.findByEmail(oldEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + oldEmail));

        existingUser.setEmail(newEmail);

        return userRepository.save(existingUser);
    }

    public User updateUserPasswordById(Long id, String newPassword){
        User existingUser=userRepository.findById(id)
                .orElseThrow(()-> new UsernameNotFoundException("User not found with ID :"+id));

        existingUser.setPassword(encoder.encode(newPassword));

        return userRepository.save(existingUser);
    }

    public User updateUserPasswordByEmail(String email, String newPassword){
        User existingUser=userRepository.findByEmail(email)
                .orElseThrow(()-> new UsernameNotFoundException("User not found with Email :"+email));

        existingUser.setPassword(encoder.encode(newPassword));

        return userRepository.save(existingUser);
    }//

    //  DELETE (by user)
    public String deleteUserByEmail(String email) {
        if (userRepository.existsByEmail(email)) {
            userRepository.deleteByEmail(email);
            return "User deleted successfully";
        } else {
            throw new UsernameNotFoundException("User not found with email: " + email);
        }
    }

    //  DELETE (by ID)
    public String deleteUserById(Long id) {
        if (userRepository.existsById(id)) {
            userRepository.deleteById(id);
            return "deleted successfully";
        } else {
            throw new UsernameNotFoundException("User not found with id: " + id);
        }
    }

    //  Suspend User
    public String suspendUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + id));

        user.setLocked(true);
        user.setExpired(true);

        userRepository.save(user);

        return "User Suspend successfully";
    }

    public String suspendUserByEmail(String email){
        User user=userRepository.findByEmail(email)
                .orElseThrow(()-> new UsernameNotFoundException( "User not found with email"+email));

        user.setExpired(true);
        user.setLocked(true);

        userRepository.save(user);
        return "User Suspend successfully";
    }

    //  VERIFY LOGIN
    public Map<String,Object> verifyLoginByEmail(String email, String password) {
        Authentication authentication = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, password)
        );

        if (authentication.isAuthenticated()) {
            User foundUser = userRepository.findByEmail(email)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));
            if (!foundUser.isIs_enable() || foundUser.isLocked() || foundUser.isExpired() ){
                throw new UserLockedException("Login failed due to user is locked");
            }
            else {
                foundUser.setExpired(false);
                foundUser.setIs_enable(true);
                userRepository.save(foundUser);

                String token= jwtService.jwtToken(email, foundUser.getRole());
                return Map.of(
                        "token", token,
                        "user", foundUser
                );
            }
        }

        throw new RuntimeException("Login failed");
    }//todo verify Login by Id

    //todo use frontend to logout user

    // VERIFY Token
    @Transactional(propagation = Propagation.REQUIRED)
    public Boolean verifyTokenAndOTP(String email ,String token, int otp) {
        try {
            Conformation conformation=redisService.get(email,Conformation.class);
            if ( conformation !=null &&
                    otp == conformation.getOtp() &&
                    userRepository.existsByEmail(conformation.getUser().getEmail())
            ) {
                userRepository.updateIsEnableLockedExpiredToTrue(conformation.getUser().getEmail());

                redisService.delete(token);//delete conformation after verifying token and otp

                return Boolean.TRUE;
            }
            else {
                return Boolean.FALSE;
            }
        }
        catch (Exception e){
            System.err.println("Error in verify Token in UserServices "+e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public List<User> listOfUserByRole(String role){
        return userRepository.findByRole(role);
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
