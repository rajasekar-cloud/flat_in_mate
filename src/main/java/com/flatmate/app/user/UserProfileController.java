package com.flatmate.app.user;

import com.flatmate.app.auth.AuthService;
import com.flatmate.app.auth.RoleChangeHistory;
import com.flatmate.app.auth.User;
import com.flatmate.app.listing.ListingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/profiles")
@RequiredArgsConstructor
public class UserProfileController {

    private final UserProfileService userProfileService;
    private final AuthService authService;
    private final ListingService listingService;

    @GetMapping("/{userId}")
    public ResponseEntity<User> getProfile(@PathVariable String userId) {
        return ResponseEntity.ok(userProfileService.getProfile(userId));
    }

    @PostMapping
    public ResponseEntity<?> updateProfile(@RequestBody User profile) {
        try {
            userProfileService.updateProfile(profile);
            return ResponseEntity.ok("Profile updated successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/kyc")
    public ResponseEntity<?> completeKyc(@RequestBody KycUpdateRequest request) {
        try {
            userProfileService.completeKyc(request);
            return ResponseEntity.ok("KYC completed successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{userId}/kyc")
    public ResponseEntity<?> deleteKycAsset(@PathVariable String userId, @RequestParam String type) {
        try {
            userProfileService.deleteKycAsset(userId, type);
            return ResponseEntity.ok("KYC asset deleted successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{userId}/kyc/upload-url")
    public ResponseEntity<Map<String, String>> getKycUploadUrl(
            @PathVariable String userId,
            @RequestParam String fileName,
            @RequestParam String type) {
        String normalizedType = normalizeKycUploadType(type);
        String safeUserId = sanitizePathSegment(userId);
        String filePath = "kyc/" + safeUserId + "/" + normalizedType + "/" + fileName;
        String url = listingService.generateUploadUrl(filePath);
        return ResponseEntity.ok(Map.of(
                "url", url,
                "filePath", filePath,
                "type", normalizedType));
    }

    @PostMapping("/{userId}/owner")
    public ResponseEntity<?> registerAsOwner(@PathVariable String userId) {
        try {
            userProfileService.registerAsOwner(userId);
            return ResponseEntity.ok("User registered as Owner successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Returns full history of all role switches for this user
    @GetMapping("/{userId}/role-history")
    public ResponseEntity<List<RoleChangeHistory>> getRoleHistory(@PathVariable String userId) {
        return ResponseEntity.ok(authService.getRoleHistory(userId));
    }

    private String normalizeKycUploadType(String type) {
        return switch (type.trim().toUpperCase()) {
            case "DOCUMENT", "ID_CARD", "DOC" -> "document";
            case "SELFIE", "FACE" -> "selfie";
            default -> throw new RuntimeException("Invalid KYC upload type. Allowed values: document, selfie");
        };
    }

    private String sanitizePathSegment(String value) {
        return value.replaceAll("[^a-zA-Z0-9._+-]", "_");
    }
}
