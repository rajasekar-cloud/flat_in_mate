package com.flatmate.app.auth;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private String accessToken;
    private String refreshToken;
    private String userId;
    private boolean isNewUser;
    private long accessTokenExpiresIn; // seconds (86400 = 24h)
}
