package com.project.skin_me;

import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

@SpringBootApplication
@EnableMethodSecurity
public class SkinMeApplication {

    public static void main(String[] args) {
        SpringApplication.run(SkinMeApplication.class, args);
    }
}
