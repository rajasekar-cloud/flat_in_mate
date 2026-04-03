package com.flatmate.app.location;

import com.flatmate.app.listing.Listing;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NearbyListingResponse {

    /** The full listing details. */
    private Listing listing;

    /**
     * Display name of the owner (firstName + lastName, falling back to
     * the legacy 'name' field, and finally to their userId if nothing is set).
     */
    private String ownerUsername;
}
