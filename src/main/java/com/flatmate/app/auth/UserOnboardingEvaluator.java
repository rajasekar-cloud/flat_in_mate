package com.flatmate.app.auth;

import com.flatmate.app.user.SeekerProfile;

import java.util.Set;

public final class UserOnboardingEvaluator {

    private UserOnboardingEvaluator() {
    }

    public static String resolveActiveRole(User user) {
        if (user.getActiveRole() != null && !user.getActiveRole().isBlank()) {
            return user.getActiveRole();
        }

        Set<String> roles = user.getRoles();
        if (roles == null || roles.isEmpty()) {
            return null;
        }
        if (roles.contains("OWNER")) {
            return "OWNER";
        }
        if (roles.contains("SEEKER")) {
            return "SEEKER";
        }
        return null;
    }

    public static boolean isOnboardingCompleteForActiveRole(User user) {
        return isOnboardingCompleteForRole(user, resolveActiveRole(user));
    }

    public static boolean isOnboardingCompleteForRole(User user, String role) {
        if (!user.isRoleSelectionComplete() || !isBasicProfileComplete(user) || !isKycComplete(user)) {
            return false;
        }

        if (role == null || role.isBlank()) {
            return false;
        }

        return switch (role.trim().toUpperCase()) {
            case "OWNER" -> hasRole(user, "OWNER") && user.isOwnerOnboardingComplete();
            case "SEEKER" -> hasRole(user, "SEEKER") && isSeekerProfileComplete(user);
            default -> false;
        };
    }

    private static boolean hasRole(User user, String role) {
        Set<String> roles = user.getRoles();
        return roles != null && roles.contains(role);
    }

    private static boolean isBasicProfileComplete(User user) {
        return hasText(user.getFirstName())
                && hasText(user.getLastName())
                && hasText(user.getDateOfBirth())
                && hasText(user.getGender());
    }

    private static boolean isKycComplete(User user) {
        if (!user.isKycComplete() || !hasText(user.getKycDocumentType())) {
            return false;
        }
        // SurePass verified path — government DB confirmed, no image URLs required
        if (user.isKycSurepassVerified()) {
            return true;
        }
        // Manual upload path — both S3 image URLs must be present
        return hasText(user.getKycDocumentImageUrl())
                && hasText(user.getKycSelfieImageUrl());
    }

    private static boolean isSeekerProfileComplete(User user) {
        SeekerProfile seekerProfile = user.getSeekerProfile();
        return seekerProfile != null
                && hasText(seekerProfile.getEducation())
                && hasText(seekerProfile.getJobTitle())
                && hasText(seekerProfile.getCompanyName())
                && seekerProfile.getKnownLanguages() != null
                && !seekerProfile.getKnownLanguages().isEmpty()
                && hasText(seekerProfile.getSmokingHabit())
                && hasText(seekerProfile.getDrinkingHabit())
                && hasText(seekerProfile.getFoodHabit())
                && hasText(seekerProfile.getMaritalStatus())
                && hasText(seekerProfile.getPetHabit())
                && seekerProfile.getLocation() != null;
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
