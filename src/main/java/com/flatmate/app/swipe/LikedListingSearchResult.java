package com.flatmate.app.swipe;

import com.flatmate.app.listing.Listing;

public class LikedListingSearchResult {

    private Listing listing;
    private String likedAt;

    public Listing getListing() { return listing; }
    public void setListing(Listing listing) { this.listing = listing; }
    public String getLikedAt() { return likedAt; }
    public void setLikedAt(String likedAt) { this.likedAt = likedAt; }
}
