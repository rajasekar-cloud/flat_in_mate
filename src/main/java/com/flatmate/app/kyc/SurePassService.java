package com.flatmate.app.kyc;

import com.flatmate.app.auth.User;
import com.flatmate.app.auth.UserOnboardingEvaluator;
import com.flatmate.app.auth.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import org.springframework.web.reactive.function.client.WebClient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SurePassService {

    private static final Logger log = LoggerFactory.getLogger(SurePassService.class);
    private final WebClient surePassWebClient;
    private final UserRepository userRepository;

    // ────────────────────────────────────────────────────────────────────────
    // AADHAAR — Step 1: Generate OTP
    // POST /aadhaar-v2/generate-otp
    // Returns { clientId } to be passed back to frontend and used in Step 2
    // ────────────────────────────────────────────────────────────────────────
    public Map<String, Object> generateAadhaarOtp(String aadhaarNumber) {
        // Only log last 4 digits for compliance
        log.info("Generating Aadhaar OTP for aadhaar ending in ****{}",
                aadhaarNumber.substring(aadhaarNumber.length() - 4));

        Map<String, String> body = Map.of("id_number", aadhaarNumber);

        Map<?, ?> response = callSurePass("/aadhaar-v2/generate-otp", body);

        if (!Boolean.TRUE.equals(response.get("success"))) {
            throw new RuntimeException("SurePass: Failed to generate Aadhaar OTP — " + response.get("message"));
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) response.get("data");

        // Return only the client_id needed for Step 2 — never return raw Aadhaar data
        return Map.of("clientId", data.get("client_id"));
    }

    // ────────────────────────────────────────────────────────────────────────
    // AADHAAR — Step 2: Verify OTP
    // POST /aadhaar-v2/submit-otp
    // On success, marks user kycComplete = true with verified name from UIDAI
    // ────────────────────────────────────────────────────────────────────────
    public void verifyAadhaarOtp(String userId, String clientId, String otp) {
        log.info("Verifying Aadhaar OTP for userId={}", userId);

        Map<String, String> body = Map.of("client_id", clientId, "otp", otp);

        Map<?, ?> response = callSurePass("/aadhaar-v2/submit-otp", body);

        if (!Boolean.TRUE.equals(response.get("success"))) {
            throw new RuntimeException("Aadhaar OTP verification failed: " + response.get("message"));
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) response.get("data");

        String verifiedName = (String) data.get("full_name");
        markKycComplete(userId, "AADHAR_CARD", verifiedName);
    }

    // ────────────────────────────────────────────────────────────────────────
    // PAN — Instant Verification (no OTP required)
    // POST /pan/pan
    // ────────────────────────────────────────────────────────────────────────
    public void verifyPan(String userId, String panNumber) {
        log.info("Verifying PAN ending in **** for userId={}", userId);

        Map<String, String> body = Map.of("id_number", panNumber);

        Map<?, ?> response = callSurePass("/pan/pan", body);

        if (!Boolean.TRUE.equals(response.get("success"))) {
            throw new RuntimeException("PAN verification failed: " + response.get("message"));
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) response.get("data");

        String verifiedName = (String) data.get("name");
        markKycComplete(userId, "PAN_CARD", verifiedName);
    }

    // ────────────────────────────────────────────────────────────────────────
    // DRIVING LICENCE — Instant Verification
    // POST /driving-license/driving-license
    // ────────────────────────────────────────────────────────────────────────
    public void verifyDrivingLicence(String userId, String dlNumber, String dob) {
        log.info("Verifying Driving Licence for userId={}", userId);

        Map<String, String> body = Map.of("id_number", dlNumber, "dob", dob);

        Map<?, ?> response = callSurePass("/driving-license/driving-license", body);

        if (!Boolean.TRUE.equals(response.get("success"))) {
            throw new RuntimeException("Driving Licence verification failed: " + response.get("message"));
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) response.get("data");

        String verifiedName = (String) data.get("name");
        markKycComplete(userId, "DRIVING_LICENSE", verifiedName);
    }

    // ────────────────────────────────────────────────────────────────────────
    // VOTER ID — Instant Verification
    // POST /voter-id/voter-id
    // ────────────────────────────────────────────────────────────────────────
    public void verifyVoterId(String userId, String voterId) {
        log.info("Verifying Voter ID for userId={}", userId);

        Map<String, String> body = Map.of("id_number", voterId);

        Map<?, ?> response = callSurePass("/voter-id/voter-id", body);

        if (!Boolean.TRUE.equals(response.get("success"))) {
            throw new RuntimeException("Voter ID verification failed: " + response.get("message"));
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) response.get("data");

        String verifiedName = (String) data.get("name");
        markKycComplete(userId, "VOTER_ID", verifiedName);
    }

    // ────────────────────────────────────────────────────────────────────────
    // Internal helpers
    // ────────────────────────────────────────────────────────────────────────

    /**
     * Calls SurePass API. Wraps WebClientResponseException into a clean RuntimeException.
     */
    private Map<?, ?> callSurePass(String path, Object body) {
        try {
            return surePassWebClient.post()
                    .uri(path)
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();
        } catch (WebClientResponseException e) {
            log.error("SurePass API error — status={} path={} body={}", e.getStatusCode(), path, e.getResponseBodyAsString());
            if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                throw new RuntimeException("SurePass authentication failed. Check your API token.");
            }
            if (e.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS) {
                throw new RuntimeException("SurePass rate limit exceeded. Please try again later.");
            }
            throw new RuntimeException("KYC verification service error: " + e.getMessage());
        }
    }

    /**
     * Marks a user as KYC-verified after successful SurePass response.
     * Sets kycSurepassVerified = true to distinguish from manual-upload flow.
     */
    private void markKycComplete(String userId, String documentType, String verifiedName) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));

        user.setKycDocumentType(documentType);
        user.setKycVerifiedName(verifiedName);
        user.setKycSurepassVerified(true);
        user.setKycComplete(true);
        user.setKycCompletedAt(LocalDateTime.now().toString());

        // Recalculate onboarding status — same pattern as UserProfileService
        user.setOnboardingComplete(UserOnboardingEvaluator.isOnboardingCompleteForActiveRole(user));

        userRepository.save(user);
        log.info("KYC marked complete for userId={} via documentType={}", userId, documentType);
    }
}
