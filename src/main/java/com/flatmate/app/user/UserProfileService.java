package com.flatmate.app.user;

import com.flatmate.app.auth.User;
import com.flatmate.app.auth.UserOnboardingEvaluator;
import com.flatmate.app.auth.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserProfileService {

    private final UserRepository userRepository;
    private final CounterRepository counterRepository;

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
        user.setKycDocumentType(normalizeDocumentType(request.getDocumentType()));
        user.setKycDocumentImageUrl(request.getDocumentImageUrl().trim());
        user.setKycSelfieImageUrl(request.getSelfieImageUrl().trim());
        user.setKycComplete(true);
        user.setKycCompletedAt(LocalDateTime.now().toString());
        updateOnboardingStatus(user);
        userRepository.save(user);
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
