package com.flatmate.app.user;

import com.flatmate.app.location.UserLocation;
import com.flatmate.app.user.enums.*;
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
    private SmokingHabit smokingHabit;
    private DrinkingHabit drinkingHabit;
    private FoodHabit foodHabit;
    private MaritalStatus maritalStatus;
    private PetHabit petHabit;
    
    // Map Location
    private UserLocation location;
}
