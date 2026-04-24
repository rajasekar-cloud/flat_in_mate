package com.flatmate.app.kyc;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class SurePassConfig {

    @Value("${surepass.base-url}")
    private String baseUrl;

    @Value("${surepass.token}")
    private String token;

    @Bean
    public WebClient surePassWebClient() {
        return WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("Authorization", "Bearer " + token)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }
}
