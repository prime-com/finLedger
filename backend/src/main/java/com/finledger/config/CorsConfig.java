package com.finledger.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Value("${app.cors.allowed-origins:http://localhost:4200}")
    private String allowedOrigins;

    @Value("${FRONTEND_URL:}")
    private String frontendUrl;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        List<String> origins = new ArrayList<>();

        // Always permit local development frontend
        origins.add("http://localhost:4200");

        // Parse comma-separated origins from app.cors.allowed-origins
        if (allowedOrigins != null && !allowedOrigins.isBlank()) {
            for (String origin : allowedOrigins.split(",")) {
                String clean = origin.trim();
                if (!clean.isEmpty() && !origins.contains(clean)) {
                    origins.add(clean);
                }
            }
        }

        // Add explicit FRONTEND_URL environment variable from hosting platform (e.g. Render)
        if (frontendUrl != null && !frontendUrl.isBlank()) {
            String clean = frontendUrl.trim();
            // Remove trailing slash if provided (e.g. https://finledger.vercel.app/ -> https://finledger.vercel.app)
            if (clean.endsWith("/")) {
                clean = clean.substring(0, clean.length() - 1);
            }
            if (!clean.isEmpty() && !origins.contains(clean)) {
                origins.add(clean);
            }
        }

        registry.addMapping("/api/**")
                .allowedOrigins(origins.toArray(new String[0]))
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
}
