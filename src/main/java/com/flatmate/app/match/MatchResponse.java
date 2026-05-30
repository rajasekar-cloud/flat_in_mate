package com.flatmate.app.match;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MatchResponse {
    private String matchId;
    private String otherUserId;
    private String otherUserName;
    private String otherUserProfilePic;
    private String listingId;
    private String status;
    private String createdAt;
}
