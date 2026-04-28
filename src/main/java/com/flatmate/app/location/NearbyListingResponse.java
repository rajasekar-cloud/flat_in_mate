package com.flatmate.app.location;

import com.flatmate.app.listing.Listing;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
public class NearbyListingResponse {

    private Listing listing;
    private String ownerUsername;

    public Listing getListing() { return listing; }
    public void setListing(Listing listing) { this.listing = listing; }
    public String getOwnerUsername() { return ownerUsername; }
    public void setOwnerUsername(String ownerUsername) { this.ownerUsername = ownerUsername; }
}
