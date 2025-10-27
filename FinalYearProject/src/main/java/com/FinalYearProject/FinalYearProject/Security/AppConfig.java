package com.FinalYearProject.FinalYearProject.Security;

import com.FinalYearProject.FinalYearProject.Domain.Conformation;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

    @Bean
    public Conformation conformation() {
        return new Conformation();
    }
}
