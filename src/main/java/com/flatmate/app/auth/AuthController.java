package com.flatmate.app.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // ── Send OTP ──────────────────────────────────────────────────────────────
    @PostMapping("/otp/send")
    public ResponseEntity<String> sendOtp(@RequestBody Map<String, String> request) {
        authService.sendOtp(request.get("phone"));
        return ResponseEntity.ok("OTP sent successfully");
    }

    // ── Verify OTP → returns AuthResponse with isNewUser flag ────────────────
    @PostMapping("/otp/verify")
    public ResponseEntity<?> verifyOtp(@RequestBody Map<String, String> request) {
        try {
            AuthResponse response = authService.verifyOtp(request.get("phone"), request.get("otp"));
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ── Google Sign-In ────────────────────────────────────────────────────────
    @PostMapping("/social/google")
    public ResponseEntity<?> googleLogin(@RequestBody Map<String, String> request) {
        try {
            AuthResponse response = authService.loginWithGoogle(request.get("idToken"));
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ── Apple Sign-In ─────────────────────────────────────────────────────────
    @PostMapping("/social/apple")
    public ResponseEntity<?> appleLogin(@RequestBody Map<String, String> request) {
        try {
            AuthResponse response = authService.loginWithApple(request.get("appleId"), request.get("email"));
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ── Refresh Access Token ──────────────────────────────────────────────────
    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@RequestBody Map<String, String> request) {
        try {
            AuthResponse response = authService.refreshAccessToken(request.get("refreshToken"));
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ── Set Role (post-login onboarding) ──────────────────────────────────────
    // Call this after login if isNewUser=true to show "Seeker or Owner?" screen.
    // Also call this when user wants to switch their role.
    @PostMapping("/set-role")
    public ResponseEntity<?> setRole(@RequestBody Map<String, String> request) {
        try {
            String role = request.get("role");
            authService.setUserRole(request.get("userId"), role);
            return ResponseEntity.ok(Map.of(
                    "message", "Role set successfully",
                    "role", role.toUpperCase()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
