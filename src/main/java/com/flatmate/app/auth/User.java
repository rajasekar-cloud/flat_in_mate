package com.flatmate.app.auth;

import lombok.*;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.*;

import com.flatmate.app.user.SeekerProfile;

import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@DynamoDbBean
public class User {

    private String userId;
    private String phone;
    private String email;
    private String password;
    
    // Legacy mapping or full name
    private String name;
    
    // New Basic Info
    private String firstName;
    private String lastName;
    private String dateOfBirth; // YYYY-MM-DD
    private String gender;

    // Profile specifics
    private SeekerProfile seekerProfile;
    
    private String profilePic;
    private String bio;
    private Set<String> roles;
    private String activeRole;
    private boolean isPremium;

    // Onboarding fields
    private boolean roleSelectionComplete; // true once user selects SEEKER or OWNER
    private boolean onboardingComplete; // true once role-specific data + KYC are completed
    private String roleConfirmedAt; // ISO timestamp when role was confirmed
    private boolean ownerOnboardingComplete; // true once owner creates their first listing
    private String kycDocumentType; // AADHAR_CARD, DRIVING_LICENSE, VOTER_ID, PASSPORT, PAN_CARD
    private String kycDocumentImageUrl;
    private String kycSelfieImageUrl;
    private boolean kycComplete;
    private String kycCompletedAt;

    // SurePass verification fields
    private String kycVerifiedName;       // Name returned from govt DB via SurePass
    private boolean kycSurepassVerified;  // true = verified via SurePass API (not just image upload)

    @DynamoDbPartitionKey
    @DynamoDbAttribute("PK")
    public String getPk() {
        return "USER#" + userId;
    }

    public void setPk(String pk) {
        // Required for DynamoDB Mapper
    }

    @DynamoDbSortKey
    @DynamoDbAttribute("SK")
    public String getSk() {
        return "METADATA";
    }

    public void setSk(String sk) {
        // Required for DynamoDB Mapper
    }
}
