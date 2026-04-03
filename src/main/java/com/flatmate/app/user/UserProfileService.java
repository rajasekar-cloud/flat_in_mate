package com.flatmate.app.user;

import com.flatmate.app.auth.User;
import com.flatmate.app.auth.UserOnboardingEvaluator;
import com.flatmate.app.auth.UserRepository;
import com.flatmate.app.listing.ListingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserProfileService {

    private final UserRepository userRepository;
    private final CounterRepository counterRepository;
    private final ListingService listingService;

    public void updateProfile(User profileUpdates) {
        if (profileUpdates.getUserId() == null) {
            throw new RuntimeException("User ID is required for updating profile");
        }
        
        User existingUser = getProfile(profileUpdates.getUserId());
        
        // Basic Info Updates
        if (profileUpdates.getFirstName() != null) existingUser.setFirstName(profileUpdates.getFirstName());
        if (profileUpdates.getLastName() != null) existingUser.setLastName(profileUpdates.getLastName());
        if (profileUpdates.getDateOfBirth() != null) existingUser.setDateOfBirth(profileUpdates.getDateOfBirth());
        if (profileUpdates.getGender() != null) existingUser.setGender(profileUpdates.getGender());
        if (profileUpdates.getProfilePic() != null) existingUser.setProfilePic(profileUpdates.getProfilePic());
        if (profileUpdates.getBio() != null) existingUser.setBio(profileUpdates.getBio());
        if (profileUpdates.getName() != null) existingUser.setName(profileUpdates.getName()); // Legacy fallback
        
        // Seeker Profile Updates (Deep Partial Update)
        if (profileUpdates.getSeekerProfile() != null) {
            if (existingUser.getSeekerProfile() == null) {
                existingUser.setSeekerProfile(profileUpdates.getSeekerProfile());
            } else {
                SeekerProfile existingSeeker = existingUser.getSeekerProfile();
                SeekerProfile newSeeker = profileUpdates.getSeekerProfile();
                
                if (newSeeker.getEducation() != null) existingSeeker.setEducation(newSeeker.getEducation());
                if (newSeeker.getJobTitle() != null) existingSeeker.setJobTitle(newSeeker.getJobTitle());
                if (newSeeker.getCompanyName() != null) existingSeeker.setCompanyName(newSeeker.getCompanyName());
                if (newSeeker.getKnownLanguages() != null) existingSeeker.setKnownLanguages(newSeeker.getKnownLanguages());
                
                // Lifestyle Habits
                if (newSeeker.getSmokingHabit() != null) existingSeeker.setSmokingHabit(newSeeker.getSmokingHabit());
                if (newSeeker.getDrinkingHabit() != null) existingSeeker.setDrinkingHabit(newSeeker.getDrinkingHabit());
                if (newSeeker.getFoodHabit() != null) existingSeeker.setFoodHabit(newSeeker.getFoodHabit());
                if (newSeeker.getMaritalStatus() != null) existingSeeker.setMaritalStatus(newSeeker.getMaritalStatus());
                if (newSeeker.getPetHabit() != null) existingSeeker.setPetHabit(newSeeker.getPetHabit());
                if (newSeeker.getDescription() != null) existingSeeker.setDescription(newSeeker.getDescription());
                
                // Location
                if (newSeeker.getLocation() != null) existingSeeker.setLocation(newSeeker.getLocation());
            }
        }

        updateOnboardingStatus(existingUser);
        userRepository.save(existingUser);
    }

    public User getProfile(String userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public void registerAsOwner(String userId) {
        User user = getProfile(userId);

        // Atomic check for 5000 limit
        if (!counterRepository.incrementCounterIfUnderLimit("OWNERS", 5000)) {
            if (!user.isPremium()) {
                throw new RuntimeException("Owner limit reached. Subscription required to list.");
            }
        }

        user.getRoles().add("OWNER");
        updateOnboardingStatus(user);
        userRepository.save(user);
    }

    public void completeKyc(KycUpdateRequest request) {
        if (request.getUserId() == null || request.getUserId().isBlank()) {
            throw new RuntimeException("User ID is required for KYC");
        }
        if (request.getDocumentType() == null || request.getDocumentType().isBlank()) {
            throw new RuntimeException("Document type is required for KYC");
        }
        if (request.getDocumentImageUrl() == null || request.getDocumentImageUrl().isBlank()) {
            throw new RuntimeException("Document image URL is required for KYC");
        }
        if (request.getSelfieImageUrl() == null || request.getSelfieImageUrl().isBlank()) {
            throw new RuntimeException("Selfie image URL is required for KYC");
        }

        User user = getProfile(request.getUserId());
        deleteReplacedKycFile(user.getKycDocumentImageUrl(), request.getDocumentImageUrl());
        deleteReplacedKycFile(user.getKycSelfieImageUrl(), request.getSelfieImageUrl());
        user.setKycDocumentType(normalizeDocumentType(request.getDocumentType()));
        user.setKycDocumentImageUrl(request.getDocumentImageUrl().trim());
        user.setKycSelfieImageUrl(request.getSelfieImageUrl().trim());
        user.setKycComplete(true);
        user.setKycCompletedAt(LocalDateTime.now().toString());
        updateOnboardingStatus(user);
        userRepository.save(user);
    }

    public void deleteKycAsset(String userId, String type) {
        User user = getProfile(userId);
        String normalizedType = normalizeKycAssetType(type);

        if ("document".equals(normalizedType) || "all".equals(normalizedType)) {
            listingService.deleteObjectByUrl(user.getKycDocumentImageUrl());
            user.setKycDocumentImageUrl(null);
            user.setKycDocumentType(null);
        }

        if ("selfie".equals(normalizedType) || "all".equals(normalizedType)) {
            listingService.deleteObjectByUrl(user.getKycSelfieImageUrl());
            user.setKycSelfieImageUrl(null);
        }

        user.setKycComplete(hasBothKycAssets(user));
        if (!user.isKycComplete()) {
            user.setKycCompletedAt(null);
        }
        updateOnboardingStatus(user);
        userRepository.save(user);
    }

    private void deleteReplacedKycFile(String existingUrl, String newUrl) {
        if (existingUrl == null || existingUrl.isBlank() || newUrl == null || newUrl.isBlank()) {
            return;
        }

        if (!existingUrl.trim().equals(newUrl.trim())) {
            listingService.deleteObjectByUrl(existingUrl);
        }
    }

    private String normalizeKycAssetType(String type) {
        return switch (type.trim().toUpperCase()) {
            case "DOCUMENT", "ID_CARD", "DOC" -> "document";
            case "SELFIE", "FACE" -> "selfie";
            case "ALL" -> "all";
            default -> throw new RuntimeException("Invalid KYC asset type. Allowed values: document, selfie, all");
        };
    }

    private boolean hasBothKycAssets(User user) {
        return user.getKycDocumentImageUrl() != null
                && !user.getKycDocumentImageUrl().isBlank()
                && user.getKycSelfieImageUrl() != null
                && !user.getKycSelfieImageUrl().isBlank();
    }

    private void updateOnboardingStatus(User user) {
        user.setOnboardingComplete(UserOnboardingEvaluator.isOnboardingCompleteForActiveRole(user));
    }

    private String normalizeDocumentType(String documentType) {
        return switch (documentType.trim().toUpperCase()) {
            case "AADHAR", "AADHAR_CARD" -> "AADHAR_CARD";
            case "DRIVING_LICENCE", "DRIVING_LICENSE" -> "DRIVING_LICENSE";
            case "VOTER", "VOTER_ID" -> "VOTER_ID";
            case "PASSPORT" -> "PASSPORT";
            default -> throw new RuntimeException(
                    "Invalid document type. Allowed values: AADHAR_CARD, DRIVING_LICENSE, VOTER_ID, PASSPORT");
        };
    }
}
