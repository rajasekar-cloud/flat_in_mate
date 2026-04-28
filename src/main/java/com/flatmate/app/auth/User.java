package com.flatmate.app.auth;

import lombok.*;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.*;
import com.flatmate.app.user.SeekerProfile;
import java.util.Set;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@DynamoDbBean
public class User {

    private String userId;
    private String phone;
    private String email;
    private String password;
    private String name;
    private String firstName;
    private String lastName;
    private String dateOfBirth;
    private String gender;
    private SeekerProfile seekerProfile;
    private String profilePic;
    private String bio;
    private Set<String> roles;
    private String activeRole;
    private boolean isPremium;
    private boolean roleSelectionComplete;
    private boolean onboardingComplete;
    private String roleConfirmedAt;
    private boolean ownerOnboardingComplete;
    private String kycDocumentType;
    private String kycDocumentImageUrl;
    private String kycSelfieImageUrl;
    private boolean kycComplete;
    private String kycCompletedAt;
    private String kycVerifiedName;
    private boolean kycSurepassVerified;

    // Getters
    public String getUserId() { return userId; }
    public String getPhone() { return phone; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public String getName() { return name; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getDateOfBirth() { return dateOfBirth; }
    public String getGender() { return gender; }
    public SeekerProfile getSeekerProfile() { return seekerProfile; }
    public String getProfilePic() { return profilePic; }
    public String getBio() { return bio; }
    public Set<String> getRoles() { return roles; }
    public String getActiveRole() { return activeRole; }
    public boolean isPremium() { return isPremium; }
    public boolean isRoleSelectionComplete() { return roleSelectionComplete; }
    public boolean isOnboardingComplete() { return onboardingComplete; }
    public String getRoleConfirmedAt() { return roleConfirmedAt; }
    public boolean isOwnerOnboardingComplete() { return ownerOnboardingComplete; }
    public String getKycDocumentType() { return kycDocumentType; }
    public String getKycDocumentImageUrl() { return kycDocumentImageUrl; }
    public String getKycSelfieImageUrl() { return kycSelfieImageUrl; }
    public boolean isKycComplete() { return kycComplete; }
    public String getKycCompletedAt() { return kycCompletedAt; }
    public String getKycVerifiedName() { return kycVerifiedName; }
    public boolean isKycSurepassVerified() { return kycSurepassVerified; }

    // Setters
    public void setUserId(String userId) { this.userId = userId; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setEmail(String email) { this.email = email; }
    public void setPassword(String password) { this.password = password; }
    public void setName(String name) { this.name = name; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public void setDateOfBirth(String dateOfBirth) { this.dateOfBirth = dateOfBirth; }
    public void setGender(String gender) { this.gender = gender; }
    public void setSeekerProfile(SeekerProfile seekerProfile) { this.seekerProfile = seekerProfile; }
    public void setProfilePic(String profilePic) { this.profilePic = profilePic; }
    public void setBio(String bio) { this.bio = bio; }
    public void setRoles(Set<String> roles) { this.roles = roles; }
    public void setActiveRole(String activeRole) { this.activeRole = activeRole; }
    public void setPremium(boolean premium) { isPremium = premium; }
    public void setRoleSelectionComplete(boolean roleSelectionComplete) { this.roleSelectionComplete = roleSelectionComplete; }
    public void setOnboardingComplete(boolean onboardingComplete) { this.onboardingComplete = onboardingComplete; }
    public void setRoleConfirmedAt(String roleConfirmedAt) { this.roleConfirmedAt = roleConfirmedAt; }
    public void setOwnerOnboardingComplete(boolean ownerOnboardingComplete) { this.ownerOnboardingComplete = ownerOnboardingComplete; }
    public void setKycDocumentType(String kycDocumentType) { this.kycDocumentType = kycDocumentType; }
    public void setKycDocumentImageUrl(String kycDocumentImageUrl) { this.kycDocumentImageUrl = kycDocumentImageUrl; }
    public void setKycSelfieImageUrl(String kycSelfieImageUrl) { this.kycSelfieImageUrl = kycSelfieImageUrl; }
    public void setKycComplete(boolean kycComplete) { this.kycComplete = kycComplete; }
    public void setKycCompletedAt(String kycCompletedAt) { this.kycCompletedAt = kycCompletedAt; }
    public void setKycVerifiedName(String kycVerifiedName) { this.kycVerifiedName = kycVerifiedName; }
    public void setKycSurepassVerified(boolean kycSurepassVerified) { this.kycSurepassVerified = kycSurepassVerified; }

    @DynamoDbPartitionKey
    @DynamoDbAttribute("PK")
    public String getPk() {
        return "USER#" + userId;
    }

    public void setPk(String pk) {
    }

    @DynamoDbSortKey
    @DynamoDbAttribute("SK")
    public String getSk() {
        return "METADATA";
    }

    public void setSk(String sk) {
    }
}
