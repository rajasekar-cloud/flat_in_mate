package com.flatmate.app.match;

import com.flatmate.app.listing.Listing;

public class MatchSearchResult {

    private Match match;
    private String otherUserId;
    private String otherUserName;
    private String otherUserProfilePic;
    private Listing listing;

    public Match getMatch() { return match; }
    public void setMatch(Match match) { this.match = match; }
    public String getOtherUserId() { return otherUserId; }
    public void setOtherUserId(String otherUserId) { this.otherUserId = otherUserId; }
    public String getOtherUserName() { return otherUserName; }
    public void setOtherUserName(String otherUserName) { this.otherUserName = otherUserName; }
    public String getOtherUserProfilePic() { return otherUserProfilePic; }
    public void setOtherUserProfilePic(String otherUserProfilePic) { this.otherUserProfilePic = otherUserProfilePic; }
    public Listing getListing() { return listing; }
    public void setListing(Listing listing) { this.listing = listing; }
}
