//    package com.FinalYearProject.FinalYearProject.TestService;
//
//    import com.FinalYearProject.FinalYearProject.Domain.User;
//    import com.FinalYearProject.FinalYearProject.Repository.ConformationRepository;
//    import com.FinalYearProject.FinalYearProject.Repository.UserRepository;
//    import com.FinalYearProject.FinalYearProject.Service.ConformationService;
//    import com.FinalYearProject.FinalYearProject.Service.JwtService;
//    import com.FinalYearProject.FinalYearProject.Service.UserService;
//    import org.junit.jupiter.api.BeforeEach;
//    import org.junit.jupiter.api.Test;
//    import org.junit.jupiter.api.extension.ExtendWith;
//    import org.mockito.ArgumentMatchers;
//    import org.mockito.InjectMocks;
//    import org.mockito.Mock;
//
//    import org.mockito.Mockito.*;
//    import org.mockito.junit.jupiter.MockitoExtension;
//    import org.springframework.security.authentication.AuthenticationManager;
//    import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//    import org.springframework.security.core.Authentication;
//    import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
//
//    import static org.assertj.core.api.Fail.fail;
//    import static org.mockito.Mockito.verify;
//    import static org.junit.jupiter.api.Assertions.assertEquals;
//    import static org.mockito.ArgumentMatchers.any;
//    import static org.mockito.Mockito.*;
//
//    @ExtendWith(MockitoExtension.class)
//    public class TestUserService {
//
//        private User fakeUser;
//
//
//        @Mock
//        private  UserRepository userRepository;
//        @Mock
//        private  ConformationRepository conformationRepository;
//        @Mock
//        private  ConformationService conformationService;
//        @Mock
//        private  AuthenticationManager authManager;
//        @Mock
//        private  JwtService jwtService;
//        @Mock
//        private  BCryptPasswordEncoder encoder ;
//
//        @InjectMocks
//        private UserService userService;
//        @BeforeEach
//        void setUp() {
//            fakeUser = new User(); // initialize here
//            Authentication fakeAuth = mock(Authentication.class);
//            when(
//                    authManager.authenticate(
//                            ArgumentMatchers.argThat(
//                                    token->
//                                            token instanceof UsernamePasswordAuthenticationToken
//                                                    && token.getName().equals("hassanrafai1707@gmail.com")
//                                                    && token.getCredentials().equals("123456789")
//                            )
//                    )
//            )
//                    .thenReturn(fakeAuth);
//
//            when(jwtService.jwtToken("hassanrafai1707@gmail.com"))
//                    .thenReturn("fake-jwt-token");
//        }
//        @Test
//        void verifyLoginTestWithCorrectCredentials(){
//            fakeUser.setEmail("hassanrafai1707@gmail.com");
//            fakeUser.setPassword("123456789");
//
//            String token=userService.verifyLogin(fakeUser);
//
//            assertEquals("fake-jwt-token", token);
//            verify(authManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
//            verify(jwtService, times(1)).jwtToken("hassanrafai1707@gmail.com");
//
//        }
//        @Test
//        void verifyLoginTestWrongCredentials() {
//            // Arrange - create a fake user with wrong email
//            fakeUser.setEmail("hassanrafai17071@gmail.com");
//            fakeUser.setPassword("123456789");
//
//            // Mock behavior: authentication should fail for wrong credentials
//            when(authManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
//                    .thenThrow(new RuntimeException("Bad credentials"));
//
//            // Act & Assert
//            try {
//                userService.verifyLogin(fakeUser);
//                fail("Expected an exception for wrong credentials");
//            } catch (Exception e) {
//                assertEquals("Bad credentials", e.getMessage());
//            }
//
//            // Verify interactions
//            verify(authManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
//            verify(jwtService, never()).jwtToken(anyString()); // JWT should not be generated
//        }
//
//    }
