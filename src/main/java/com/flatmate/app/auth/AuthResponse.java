package com.flatmate.app.auth;

import lombok.*;
import java.util.Set;

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
    private long accessTokenExpiresIn;

    public String getAccessToken() { return accessToken; }
    public void setAccessToken(String accessToken) { this.accessToken = accessToken; }
    public String getRefreshToken() { return refreshToken; }
    public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public Set<String> getRoles() { return roles; }
    public void setRoles(Set<String> roles) { this.roles = roles; }
    public String getActiveRole() { return activeRole; }
    public void setActiveRole(String activeRole) { this.activeRole = activeRole; }
    public boolean isNewUser() { return isNewUser; }
    public void setNewUser(boolean newUser) { isNewUser = newUser; }
    public boolean isOnboardingComplete() { return onboardingComplete; }
    public void setOnboardingComplete(boolean onboardingComplete) { this.onboardingComplete = onboardingComplete; }
    public long getAccessTokenExpiresIn() { return accessTokenExpiresIn; }
    public void setAccessTokenExpiresIn(long accessTokenExpiresIn) { this.accessTokenExpiresIn = accessTokenExpiresIn; }
}
