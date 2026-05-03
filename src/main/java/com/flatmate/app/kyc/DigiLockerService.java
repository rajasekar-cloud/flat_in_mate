package com.flatmate.app.kyc;

import com.flatmate.app.auth.User;
import com.flatmate.app.auth.UserOnboardingEvaluator;
import com.flatmate.app.auth.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class DigiLockerService {

    private final WebClient digiLockerWebClient;
    private final UserRepository userRepository;

    @Value("${digilocker.client-id:placeholder-id}")
    private String clientId;

    @Value("${digilocker.client-secret:placeholder-secret}")
    private String clientSecret;

    @Value("${digilocker.redirect-uri:http://localhost:8081/profiles/kyc/digilocker/callback}")
    private String redirectUri;

    @Value("${digilocker.base-url:https://api.digitallocker.gov.in/public/oauth2/1}")
    private String baseUrl;

    /**
     * Step 1: Generates the URL to redirect the user to DigiLocker login.
     */
    public String getAuthorizationUrl(String userId) {
        return String.format(
            "https://digitallocker.gov.in/public/oauth2/1/authorize?response_type=code&client_id=%s&redirect_uri=%s&state=%s",
            clientId, redirectUri, userId
        );
    }

    /**
     * Step 2: Exchanges the authorization code for an access token and fetches user data.
     */
    public void verifyUser(String userId, String code) {
        log.info("Verifying DigiLocker for userId={}", userId);

        // 1. Exchange code for Token
        Map<String, Object> tokenResponse = exchangeCodeForToken(code);
        String accessToken = (String) tokenResponse.get("access_token");

        if (accessToken == null) {
            throw new RuntimeException("DigiLocker: Failed to obtain access token");
        }

        // 2. Fetch User Profile/Aadhaar Data
        // In a real implementation, you would call /eaadhaar or /profile
        // For this demo, we assume the token response contains some basic user info or we fetch it.
        String verifiedName = (String) tokenResponse.get("name"); 
        if (verifiedName == null) {
            // Fallback: Fetch profile if name not in token
            verifiedName = fetchVerifiedNameFromProfile(accessToken);
        }

        // 3. Mark KYC Complete
        markKycComplete(userId, verifiedName);
    }

    private Map<String, Object> exchangeCodeForToken(String code) {
        return digiLockerWebClient.post()
                .uri("/token")
                .bodyValue(Map.of(
                        "code", code,
                        "client_id", clientId,
                        "client_secret", clientSecret,
                        "redirect_uri", redirectUri,
                        "grant_type", "authorization_code"
                ))
                .retrieve()
                .bodyToMono(Map.class)
                .block();
    }

    private String fetchVerifiedNameFromProfile(String accessToken) {
        Map<String, Object> profile = digiLockerWebClient.get()
                .uri("/profile")
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .bodyToMono(Map.class)
                .block();
        
        return (String) profile.get("name");
    }

    private void markKycComplete(String userId, String verifiedName) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setKycVerifiedName(verifiedName);
        user.setKycComplete(true);
        user.setKycCompletedAt(LocalDateTime.now().toString());
        user.setKycSurepassVerified(false); // Mark as DigiLocker verified
        user.setKycDocumentType("DIGILOCKER_VERIFIED");

        user.setOnboardingComplete(UserOnboardingEvaluator.isOnboardingCompleteForActiveRole(user));
        userRepository.save(user);
        
        log.info("DigiLocker KYC completed for user {}: {}", userId, verifiedName);
    }
}
