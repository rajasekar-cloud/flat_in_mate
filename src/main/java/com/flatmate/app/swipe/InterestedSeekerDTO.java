package com.flatmate.app.swipe;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
public class InterestedSeekerDTO {
    private String seekerId;
    private String firstName;
    private String lastName;
    private String profilePic;
    private String gender;
    private String description;
    private String swipedAt;

    public String getSeekerId() { return seekerId; }
    public void setSeekerId(String seekerId) { this.seekerId = seekerId; }
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public String getProfilePic() { return profilePic; }
    public void setProfilePic(String profilePic) { this.profilePic = profilePic; }
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getSwipedAt() { return swipedAt; }
    public void setSwipedAt(String swipedAt) { this.swipedAt = swipedAt; }
}
