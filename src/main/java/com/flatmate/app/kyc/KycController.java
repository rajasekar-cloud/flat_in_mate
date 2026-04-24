package com.flatmate.app.kyc;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * KYC Controller — SurePass-backed document verification
 *
 * Endpoints:
 *   POST /profiles/kyc/aadhaar/generate-otp  → Step 1: sends OTP to user's Aadhaar-linked mobile
 *   POST /profiles/kyc/aadhaar/verify-otp    → Step 2: verifies OTP, marks kycComplete
 *   POST /profiles/kyc/pan/verify            → Instant PAN verification
 *   POST /profiles/kyc/dl/verify             → Instant Driving Licence verification
 *   POST /profiles/kyc/voter/verify          → Instant Voter ID verification
 *
 * All endpoints require a valid JWT (enforced by SecurityConfig → JwtFilter).
 */
@RestController
@RequestMapping("/profiles/kyc")
@RequiredArgsConstructor
public class KycController {

    private final SurePassService surePassService;

    // ── Aadhaar: Step 1 — Generate OTP ──────────────────────────────────────
    @PostMapping("/aadhaar/generate-otp")
    public ResponseEntity<?> generateAadhaarOtp(@RequestBody AadhaarOtpRequest request) {
        try {
            if (request.getAadhaarNumber() == null || request.getAadhaarNumber().isBlank()) {
                return ResponseEntity.badRequest().body(Map.of("error", "aadhaarNumber is required"));
            }
            if (!request.getAadhaarNumber().matches("\\d{12}")) {
                return ResponseEntity.badRequest().body(Map.of("error", "aadhaarNumber must be 12 digits"));
            }
            Map<String, Object> result = surePassService.generateAadhaarOtp(request.getAadhaarNumber());
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ── Aadhaar: Step 2 — Submit OTP ────────────────────────────────────────
    @PostMapping("/aadhaar/verify-otp")
    public ResponseEntity<?> verifyAadhaarOtp(@RequestBody AadhaarVerifyRequest request) {
        try {
            if (request.getUserId() == null || request.getUserId().isBlank()) {
                return ResponseEntity.badRequest().body(Map.of("error", "userId is required"));
            }
            if (request.getClientId() == null || request.getClientId().isBlank()) {
                return ResponseEntity.badRequest().body(Map.of("error", "clientId is required"));
            }
            if (request.getOtp() == null || request.getOtp().isBlank()) {
                return ResponseEntity.badRequest().body(Map.of("error", "otp is required"));
            }
            surePassService.verifyAadhaarOtp(request.getUserId(), request.getClientId(), request.getOtp());
            return ResponseEntity.ok(Map.of("message", "Aadhaar KYC verified successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ── PAN: Instant Verification ────────────────────────────────────────────
    @PostMapping("/pan/verify")
    public ResponseEntity<?> verifyPan(@RequestBody PanVerifyRequest request) {
        try {
            if (request.getUserId() == null || request.getUserId().isBlank()) {
                return ResponseEntity.badRequest().body(Map.of("error", "userId is required"));
            }
            if (request.getPanNumber() == null || request.getPanNumber().isBlank()) {
                return ResponseEntity.badRequest().body(Map.of("error", "panNumber is required"));
            }
            if (!request.getPanNumber().matches("[A-Z]{5}[0-9]{4}[A-Z]{1}")) {
                return ResponseEntity.badRequest().body(Map.of("error", "Invalid PAN format. Expected: ABCDE1234F"));
            }
            surePassService.verifyPan(request.getUserId(), request.getPanNumber());
            return ResponseEntity.ok(Map.of("message", "PAN KYC verified successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ── Driving Licence: Instant Verification ────────────────────────────────
    @PostMapping("/dl/verify")
    public ResponseEntity<?> verifyDrivingLicence(@RequestBody DlVerifyRequest request) {
        try {
            if (request.getUserId() == null || request.getUserId().isBlank()) {
                return ResponseEntity.badRequest().body(Map.of("error", "userId is required"));
            }
            if (request.getDlNumber() == null || request.getDlNumber().isBlank()) {
                return ResponseEntity.badRequest().body(Map.of("error", "dlNumber is required"));
            }
            if (request.getDob() == null || request.getDob().isBlank()) {
                return ResponseEntity.badRequest().body(Map.of("error", "dob is required (format: YYYY-MM-DD)"));
            }
            surePassService.verifyDrivingLicence(request.getUserId(), request.getDlNumber(), request.getDob());
            return ResponseEntity.ok(Map.of("message", "Driving Licence KYC verified successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ── Voter ID: Instant Verification ──────────────────────────────────────
    @PostMapping("/voter/verify")
    public ResponseEntity<?> verifyVoterId(@RequestBody VoterVerifyRequest request) {
        try {
            if (request.getUserId() == null || request.getUserId().isBlank()) {
                return ResponseEntity.badRequest().body(Map.of("error", "userId is required"));
            }
            if (request.getVoterId() == null || request.getVoterId().isBlank()) {
                return ResponseEntity.badRequest().body(Map.of("error", "voterId is required"));
            }
            surePassService.verifyVoterId(request.getUserId(), request.getVoterId());
            return ResponseEntity.ok(Map.of("message", "Voter ID KYC verified successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
