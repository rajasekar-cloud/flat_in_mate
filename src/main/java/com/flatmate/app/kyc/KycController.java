package com.flatmate.app.kyc;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * KYC Controller — Aadhaar Offline and DigiLocker verification
 */
@RestController
@RequestMapping("/profiles/kyc")
@RequiredArgsConstructor
public class KycController {

    private final AadhaarOfflineService aadhaarOfflineService;
    private final DigiLockerService digiLockerService;

    // ── Aadhaar: Offline Verification (Zero Cost) ───────────────────────────
    @PostMapping(value = "/aadhaar/verify-offline", consumes = "multipart/form-data")
    public ResponseEntity<?> verifyAadhaarOffline(
            @RequestParam("userId") String userId,
            @RequestParam("file") org.springframework.web.multipart.MultipartFile file,
            @RequestParam("shareCode") String shareCode) {
        try {
            aadhaarOfflineService.verifyOfflineKyc(userId, file, shareCode);
            return ResponseEntity.ok(Map.of("message", "Aadhaar Offline KYC verified successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ── DigiLocker: Step 1 — Get Auth URL ───────────────────────────────────
    @GetMapping("/digilocker/auth-url")
    public ResponseEntity<?> getDigiLockerAuthUrl(@RequestParam("userId") String userId) {
        String authUrl = digiLockerService.getAuthorizationUrl(userId);
        return ResponseEntity.ok(Map.of("authUrl", authUrl));
    }

    // ── DigiLocker: Step 2 — Verify Callback Code ───────────────────────────
    // The frontend receives the 'code' from DigiLocker and passes it here
    @PostMapping("/digilocker/verify")
    public ResponseEntity<?> verifyDigiLocker(
            @RequestParam("userId") String userId,
            @RequestParam("code") String code) {
        try {
            digiLockerService.verifyUser(userId, code);
            return ResponseEntity.ok(Map.of("message", "DigiLocker KYC verified successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
