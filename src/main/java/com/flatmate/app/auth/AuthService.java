package com.flatmate.app.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final RoleChangeHistoryRepository roleChangeHistoryRepository;

    public void sendOtp(String phone) {
        // TODO: Integrate Twilio / MSG91 for real SMS
        System.out.println("Sending OTP to " + phone + ": 123456");
    }

    public AuthResponse verifyOtp(String phone, String otp) {
        if (!"123456".equals(otp)) {
            throw new RuntimeException("Invalid OTP");
        }

        User user = userRepository.findById(phone).orElseGet(() -> createPhoneUser(phone));
        return buildAuthResponse(user);
    }

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

        return buildAuthResponse(user);
    }

    public void setUserRole(String userId, String role) {
        String newRole = normalizeRole(role);
        if (!Set.of("SEEKER", "OWNER").contains(newRole)) {
            throw new RuntimeException("Invalid role. Allowed values: SEEKER, OWNER, TENANT, HOST");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Set<String> currentRoles = user.getRoles() != null ? user.getRoles() : new HashSet<>();

        boolean roleAlreadyPresent = currentRoles.contains(newRole);

        if (!currentRoles.isEmpty() && !roleAlreadyPresent) {
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

        currentRoles.add(newRole);
        user.setRoles(currentRoles);
        user.setRoleSelectionComplete(true);
        if (!roleAlreadyPresent) {
            user.setOnboardingComplete(false);
        }
        user.setActiveRole(newRole);
        user.setRoleConfirmedAt(LocalDateTime.now().toString());
        userRepository.save(user);
    }

    public AuthResponse switchUserRole(String userId, String role) {
        String targetRole = normalizeRole(role);
        if (!Set.of("SEEKER", "OWNER").contains(targetRole)) {
            throw new RuntimeException("Invalid role. Allowed values: SEEKER, OWNER, TENANT, HOST");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Set<String> currentRoles = user.getRoles() != null ? user.getRoles() : Set.of();
        if (!currentRoles.contains(targetRole)) {
            throw new RuntimeException("User does not have access to role: " + targetRole);
        }

        user.setActiveRole(targetRole);
        userRepository.save(user);
        return buildAuthResponse(user);
    }

    public List<RoleChangeHistory> getRoleHistory(String userId) {
        return roleChangeHistoryRepository.findByUserId(userId);
    }

    private User createPhoneUser(String phone) {
        User newUser = User.builder()
                .userId(phone)
                .phone(phone)
                .roles(null)
                .activeRole(null)
                .isPremium(false)
                .roleSelectionComplete(false)
                .onboardingComplete(false)
                .ownerOnboardingComplete(false)
                .kycComplete(false)
                .build();
        userRepository.save(newUser);
        return newUser;
    }

    private AuthResponse buildAuthResponse(User user) {
        String accessToken = jwtUtil.generateAccessToken(user.getUserId(), user.getRoles());
        String refreshToken = jwtUtil.generateRefreshToken(user.getUserId());
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .userId(user.getUserId())
                .roles(user.getRoles())
                .activeRole(resolveActiveRole(user))
                .isNewUser(!user.isOnboardingComplete())
                .onboardingComplete(user.isOnboardingComplete())
                .accessTokenExpiresIn(24 * 3600L)
                .build();
    }

    private String resolveActiveRole(User user) {
        if (user.getActiveRole() != null && !user.getActiveRole().isBlank()) {
            return user.getActiveRole();
        }

        Set<String> roles = user.getRoles();
        if (roles == null || roles.isEmpty()) {
            return null;
        }
        if (roles.contains("OWNER")) {
            return "OWNER";
        }
        if (roles.contains("SEEKER")) {
            return "SEEKER";
        }
        return null;
    }

    private String normalizeRole(String role) {
        if (role == null) {
            return "";
        }

        return switch (role.trim().toUpperCase()) {
            case "TENANT" -> "SEEKER";
            case "HOST" -> "OWNER";
            default -> role.trim().toUpperCase();
        };
    }
}
