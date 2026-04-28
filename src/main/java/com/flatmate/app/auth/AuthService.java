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
                RoleChangeHistory history = new RoleChangeHistory();
                history.setUserId(userId);
                history.setFromRole(fromRole);
                history.setToRole(newRole);
                history.setChangedAt(LocalDateTime.now().toString());
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
        user.setOnboardingComplete(UserOnboardingEvaluator.isOnboardingCompleteForRole(user, newRole));
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
        user.setOnboardingComplete(UserOnboardingEvaluator.isOnboardingCompleteForRole(user, targetRole));
        userRepository.save(user);
        return buildAuthResponse(user);
    }

    public List<RoleChangeHistory> getRoleHistory(String userId) {
        return roleChangeHistoryRepository.findByUserId(userId);
    }

    private User createPhoneUser(String phone) {
        User newUser = new User();
        newUser.setUserId(phone);
        newUser.setPhone(phone);
        newUser.setRoles(null);
        newUser.setActiveRole(null);
        newUser.setPremium(false);
        newUser.setRoleSelectionComplete(false);
        newUser.setOnboardingComplete(false);
        newUser.setOwnerOnboardingComplete(false);
        newUser.setKycComplete(false);
        userRepository.save(newUser);
        return newUser;
    }

    private AuthResponse buildAuthResponse(User user) {
        String activeRole = UserOnboardingEvaluator.resolveActiveRole(user);
        // Read the persisted value — setUserRole / updateOnboardingStatus already computes
        // and saves this correctly. Re-evaluating here ignores whatever was stored and causes
        // onboardingComplete to return false even after the user has finished onboarding.
        boolean onboardingComplete = user.isOnboardingComplete();
        String accessToken = jwtUtil.generateAccessToken(user.getUserId(), user.getRoles());
        String refreshToken = jwtUtil.generateRefreshToken(user.getUserId());
        
        AuthResponse response = new AuthResponse();
        response.setAccessToken(accessToken);
        response.setRefreshToken(refreshToken);
        response.setUserId(user.getUserId());
        response.setRoles(user.getRoles());
        response.setActiveRole(activeRole);
        response.setNewUser(!onboardingComplete);
        response.setOnboardingComplete(onboardingComplete);
        response.setAccessTokenExpiresIn(24 * 3600L);
        return response;
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
