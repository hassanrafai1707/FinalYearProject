package com.FinalYearProject.FinalYearProject.Config.Security;

import com.FinalYearProject.FinalYearProject.Config.Security.Filter.JwtFilter;
import com.FinalYearProject.FinalYearProject.Service.MyUserDetailsServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

//this class is used to Configure custom security
@EnableMethodSecurity
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Value("${app.version}")
    private String appVersion;
    @Autowired
    private JwtFilter jwtFilter;
    @Autowired
    private MyUserDetailsServices myUserDetailsServices;

    // this Constructor is used to Customize the Security Filter Chain flow
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        return httpSecurity
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(
                        auth ->
                                auth
                                        .requestMatchers(appVersion+"/admin/**").hasAnyAuthority("ROLE_ADMIN")
                                        .requestMatchers(appVersion+"/student/**").hasAnyAuthority("ROLE_STUDENT")
                                        .requestMatchers(appVersion+"/teacher**").hasAnyAuthority("ROLE_TEACHER")
                                        .requestMatchers(appVersion+"/supervisor").hasAnyAuthority("ROLE_SUPERVISOR")
                                        .requestMatchers(
                                                // All of the below paths are permitted with put being authorised
                                                appVersion+"/login",
                                                appVersion+"/auth/**",
                                                "/login",
                                                "/register",
                                                "/css/**",
                                                "/js/**",
                                                "/images/**",
                                                "/webjars/**",
                                                "/"
                                        )
                                        .permitAll()
                                        .anyRequest()
                                        .authenticated()
                )
                // this calls the been authenticationProvider the nect function for custom logic
                .authenticationProvider(authenticationProvider())
                //this line adds the jwt custom logic class after the previous set of instruction are completed
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }
    //better practice to use this way
    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder(12);
    }
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setPasswordEncoder(passwordEncoder());
        provider.setUserDetailsService(myUserDetailsServices);
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }
}
