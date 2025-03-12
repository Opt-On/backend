package com.opton.spring_boot.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig {
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                // for (int i = 0; i < 100; i ++){
                //     System.err.println("FUCK YOU BITCH");
                // }
                // registry.addMapping("/**") // Apply to all endpoints
                //         .allowedOrigins("*") // Allow frontend
                //         .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // Allow these HTTP methods
                //         .allowedHeaders("*") // Allow all headers
                //         .allowCredentials(true); // Allow cookies/auth headers
            }
        };
    }
}
