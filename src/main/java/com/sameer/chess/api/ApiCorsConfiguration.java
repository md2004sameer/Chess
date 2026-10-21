package com.sameer.chess.api;

import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/** Enables API CORS only for origins explicitly supplied through configuration. */
@Configuration
public class ApiCorsConfiguration implements WebMvcConfigurer {
    private final List<String> allowedOrigins;

    public ApiCorsConfiguration(
            @Value("${app.cors.allowed-origins:}") String configuredOrigins) {
        List<String> origins = new ArrayList<>();
        for (String origin : configuredOrigins.split(",")) {
            String trimmedOrigin = origin.trim();
            if (!trimmedOrigin.isEmpty()) {
                origins.add(trimmedOrigin);
            }
        }
        allowedOrigins = List.copyOf(origins);
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        if (allowedOrigins.isEmpty()) return;
        registry.addMapping("/api/**")
                .allowedOrigins(allowedOrigins.toArray(String[]::new))
                .allowedMethods("GET", "POST", "OPTIONS")
                .allowedHeaders("Accept", "Content-Type")
                .exposedHeaders("Location")
                .allowCredentials(false);
    }
}
