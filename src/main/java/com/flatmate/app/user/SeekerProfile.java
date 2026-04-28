package com.flatmate.app.user;

import com.flatmate.app.location.UserLocation;
import lombok.*;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;

import java.util.List;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@DynamoDbBean
public class SeekerProfile {
    private String education;
    private String jobTitle;
    private String companyName;
    private List<String> knownLanguages;
    private String smokingHabit;
    private String drinkingHabit;
    private String foodHabit;
    private String maritalStatus;
    private String petHabit;
    private String description;
    private UserLocation location;

    public String getEducation() { return education; }
    public void setEducation(String education) { this.education = education; }
    public String getJobTitle() { return jobTitle; }
    public void setJobTitle(String jobTitle) { this.jobTitle = jobTitle; }
    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }
    public List<String> getKnownLanguages() { return knownLanguages; }
    public void setKnownLanguages(List<String> knownLanguages) { this.knownLanguages = knownLanguages; }
    public String getSmokingHabit() { return smokingHabit; }
    public void setSmokingHabit(String smokingHabit) { this.smokingHabit = smokingHabit; }
    public String getDrinkingHabit() { return drinkingHabit; }
    public void setDrinkingHabit(String drinkingHabit) { this.drinkingHabit = drinkingHabit; }
    public String getFoodHabit() { return foodHabit; }
    public void setFoodHabit(String foodHabit) { this.foodHabit = foodHabit; }
    public String getMaritalStatus() { return maritalStatus; }
    public void setMaritalStatus(String maritalStatus) { this.maritalStatus = maritalStatus; }
    public String getPetHabit() { return petHabit; }
    public void setPetHabit(String petHabit) { this.petHabit = petHabit; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public UserLocation getLocation() { return location; }
    public void setLocation(UserLocation location) { this.location = location; }
}
