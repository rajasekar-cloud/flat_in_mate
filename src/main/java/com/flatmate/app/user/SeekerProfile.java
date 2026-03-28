package com.flatmate.app.user;

import com.flatmate.app.location.UserLocation;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@DynamoDbBean
public class SeekerProfile {
    private String education;
    private String jobTitle;
    private String companyName;
    private List<String> knownLanguages;
    
    // Lifestyle
    private String smokingHabit;
    private String drinkingHabit;
    private String foodHabit;
    private String maritalStatus;
    private String petHabit;
    private String description;
    
    // Map Location
    private UserLocation location;
}
