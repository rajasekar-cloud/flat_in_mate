package com.flatmate.app.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/otp/send")
    public ResponseEntity<String> sendOtp(@RequestBody Map<String, String> request) {
        authService.sendOtp(request.get("phone"));
        return ResponseEntity.ok("OTP sent successfully");
    }

    @PostMapping("/otp/verify")
    public ResponseEntity<?> verifyOtp(@RequestBody Map<String, String> request) {
        try {
            AuthResponse response = authService.verifyOtp(request.get("phone"), request.get("otp"));
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@RequestBody Map<String, String> request) {
        try {
            AuthResponse response = authService.refreshAccessToken(request.get("refreshToken"));
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

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

    @PostMapping("/switch-role")
    public ResponseEntity<?> switchRole(@RequestBody Map<String, String> request) {
        try {
            AuthResponse response = authService.switchUserRole(request.get("userId"), request.get("role"));
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
