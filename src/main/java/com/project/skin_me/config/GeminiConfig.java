package com.project.skin_me.config;

import com.google.genai.Client;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GeminiConfig {

    @Value("${google.api.key}")
    private String apiKey;

    @Bean
    @ConditionalOnProperty(
            name = "google.api.enabled",
            havingValue = "true",
            matchIfMissing = true
    )
    public Client geminiClient() {
        return Client.builder()
                .apiKey(apiKey)
                .build();
    }
}
