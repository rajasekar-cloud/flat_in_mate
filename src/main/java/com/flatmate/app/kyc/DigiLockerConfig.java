package com.flatmate.app.kyc;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class DigiLockerConfig {

    @Value("${digilocker.base-url:https://api.digitallocker.gov.in/public/oauth2/1}")
    private String baseUrl;

    @Bean
    public WebClient digiLockerWebClient(WebClient.Builder builder) {
        return builder
                .baseUrl(baseUrl)
                .build();
    }
}
