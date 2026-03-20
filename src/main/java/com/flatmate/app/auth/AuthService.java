package com.flatmate.app.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final RoleChangeHistoryRepository roleChangeHistoryRepository;

    @org.springframework.beans.factory.annotation.Value("${google.client-id}")
    private String googleClientId;

    // ─────────────────────────────────────────
    // OTP Login
    // ─────────────────────────────────────────

    public void sendOtp(String phone) {
        // TODO: Integrate Twilio / MSG91 for real SMS
        System.out.println("Sending OTP to " + phone + ": 123456");
    }

    public AuthResponse verifyOtp(String phone, String otp) {
        if (!"123456".equals(otp)) {
            throw new RuntimeException("Invalid OTP");
        }
        boolean[] isNew = { false };
        User user = userRepository.findById(phone).orElseGet(() -> {
            isNew[0] = true;
            User newUser = User.builder()
                    .userId(phone)
                    .phone(phone)
                    .roles(null) // DynamoDB rejects empty sets — roles assigned later via /auth/set-role
                    .isPremium(false)
                    .onboardingComplete(false)
                    .build();
            userRepository.save(newUser);
            return newUser;
        });
        return buildAuthResponse(user, isNew[0]);
    }

    // ─────────────────────────────────────────
    // Google Login
    // ─────────────────────────────────────────

    public AuthResponse loginWithGoogle(String idTokenString) {
        try {
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                    new NetHttpTransport(), new GsonFactory())
                    .setAudience(Collections.singletonList(googleClientId))
                    .build();

            GoogleIdToken idToken = verifier.verify(idTokenString);
            if (idToken == null)
                throw new RuntimeException("Invalid Google ID token.");

            GoogleIdToken.Payload payload = idToken.getPayload();
            String userId = payload.getSubject();
            boolean[] isNew = { false };

            User user = userRepository.findById(userId).orElseGet(() -> {
                isNew[0] = true;
                User newUser = User.builder()
                        .userId(userId)
                        .googleId(userId)
                        .email(payload.getEmail())
                        .name((String) payload.get("name"))
                        .roles(null) // DynamoDB rejects empty sets — roles assigned later via /auth/set-role
                        .onboardingComplete(false)
                        .build();
                userRepository.save(newUser);
                return newUser;
            });
            return buildAuthResponse(user, isNew[0]);

        } catch (Exception e) {
            throw new RuntimeException("Google Auth failed: " + e.getMessage());
        }
    }

    // ─────────────────────────────────────────
    // Apple Login
    // ─────────────────────────────────────────

    public AuthResponse loginWithApple(String appleId, String email) {
        // TODO: Verify Apple JWT using Apple's JWKS endpoint
        boolean[] isNew = { false };
        User user = userRepository.findById(appleId).orElseGet(() -> {
            isNew[0] = true;
            User newUser = User.builder()
                    .userId(appleId)
                    .appleId(appleId)
                    .email(email)
                    .roles(null) // DynamoDB rejects empty sets — roles assigned later via /auth/set-role
                    .onboardingComplete(false)
                    .build();
            userRepository.save(newUser);
            return newUser;
        });
        return buildAuthResponse(user, isNew[0]);
    }

    // ─────────────────────────────────────────
    // Refresh Token
    // ─────────────────────────────────────────

    public AuthResponse refreshAccessToken(String refreshToken) {
        if (!jwtUtil.isTokenValid(refreshToken)) {
            throw new RuntimeException("Invalid or expired refresh token. Please log in again.");
        }

        io.jsonwebtoken.Claims claims = jwtUtil.validateToken(refreshToken);
        String tokenType = claims.get("type", String.class);
        if (!"REFRESH".equals(tokenType)) {
            throw new RuntimeException("Provided token is not a refresh token.");
        }

        String userId = claims.getSubject();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return buildAuthResponse(user, false);
    }

    // ─────────────────────────────────────────
    // Role Selection (Post-login onboarding)
    // ─────────────────────────────────────────

    public void setUserRole(String userId, String role) {
        String newRole = role.toUpperCase();
        if (!Set.of("SEEKER", "OWNER").contains(newRole)) {
            throw new RuntimeException("Invalid role. Allowed values: SEEKER, OWNER");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Set<String> currentRoles = user.getRoles() != null ? user.getRoles() : new HashSet<>();

        // Track role change if switching from an existing role
        if (!currentRoles.isEmpty()) {
            String fromRole = currentRoles.contains("OWNER") ? "OWNER" : "SEEKER";
            if (!fromRole.equals(newRole)) {
                RoleChangeHistory history = RoleChangeHistory.builder()
                        .userId(userId)
                        .fromRole(fromRole)
                        .toRole(newRole)
                        .changedAt(LocalDateTime.now().toString())
                        .build();
                roleChangeHistoryRepository.save(history);
            }
        }

        // Set new role (replace old primary role, keep both if already has both)
        currentRoles.add(newRole);
        user.setRoles(currentRoles);
        user.setOnboardingComplete(true);
        user.setRoleConfirmedAt(LocalDateTime.now().toString());
        userRepository.save(user);
    }

    // ─────────────────────────────────────────
    // Role History
    // ─────────────────────────────────────────

    public List<RoleChangeHistory> getRoleHistory(String userId) {
        return roleChangeHistoryRepository.findByUserId(userId);
    }

    // ─────────────────────────────────────────
    // Internal helper
    // ─────────────────────────────────────────

    private AuthResponse buildAuthResponse(User user, boolean isNewUser) {
        String accessToken = jwtUtil.generateAccessToken(user.getUserId(), user.getRoles());
        String refreshToken = jwtUtil.generateRefreshToken(user.getUserId());
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .userId(user.getUserId())
                .isNewUser(isNewUser)
                .accessTokenExpiresIn(24 * 3600L) // 86400 seconds
                .build();
    }
}
