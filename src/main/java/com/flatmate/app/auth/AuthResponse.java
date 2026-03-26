package com.flatmate.app.auth;

import lombok.*;

import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private String accessToken;
    private String refreshToken;
    private String userId;
    private Set<String> roles;
    private String activeRole;
    private boolean isNewUser;
    private boolean onboardingComplete;
    private long accessTokenExpiresIn; // seconds (86400 = 24h)
}
