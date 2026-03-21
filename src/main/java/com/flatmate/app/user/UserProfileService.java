package com.flatmate.app.user;

import com.flatmate.app.auth.User;
import com.flatmate.app.auth.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

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
        
        // Seeker Profile Updates
        if (profileUpdates.getSeekerProfile() != null) {
            existingUser.setSeekerProfile(profileUpdates.getSeekerProfile());
        }

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
        userRepository.save(user);
    }
}
