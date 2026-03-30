package com.flatmate.app.swipe;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InterestedSeekerDTO {
    private String seekerId;
    private String firstName;
    private String lastName;
    private String profilePic;
    private String gender;
    private String description; // The seeker's bio/description
    private String swipedAt;
}
