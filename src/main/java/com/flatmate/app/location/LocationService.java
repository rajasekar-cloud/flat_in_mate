package com.flatmate.app.location;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LocationService {

    private final GeoListingRepository geoListingRepository;
    private final com.flatmate.app.listing.ListingService listingService;

    public void indexListing(String listingId, double lat, double lng) {
        GeoListing geo = GeoListing.builder()
                .listingId(listingId)
                .latitude(lat)
                .longitude(lng)
                .build();
        geoListingRepository.save(geo);
    }

    /**
     * Find listings within the radius, then apply optional filters.
     * All filter params are optional — if null, that filter is skipped.
     *
     * @param lat              Seeker's current (or selected) latitude
     * @param lng              Seeker's current (or selected) longitude
     * @param radiusKm         Search radius in km (default 10)
     * @param minRent          Minimum rent filter (optional)
     * @param maxRent          Maximum rent filter (optional)
     * @param genderPreference Filter by gender: MALE / FEMALE / ANY (optional)
     * @param minRooms         Minimum rooms available (optional)
     */
    public List<com.flatmate.app.listing.Listing> findNearbyListings(
            double lat, double lng, double radiusKm,
            Double minRent, Double maxRent,
            String genderPreference, Integer minRooms) {

        return geoListingRepository.findAll().stream()
                // Skip malformed geo rows instead of failing the whole request.
                .filter(this::hasValidCoordinates)
                // 1. Filter by distance
                .filter(g -> calculateDistance(lat, lng, g.getLatitude(), g.getLongitude()) <= radiusKm)
                // 2. Fetch the full listing
                .map(g -> safeGetListing(g.getListingId()))
                .filter(Objects::nonNull)
                // 3. Filter by min rent
                .filter(l -> minRent == null || l.getRent() >= minRent)
                // 4. Filter by max rent
                .filter(l -> maxRent == null || l.getRent() <= maxRent)
                // 5. Filter by gender preference — "ANY" listings match all seekers
                .filter(l -> genderPreference == null
                        || "ANY".equalsIgnoreCase(l.getGenderPreference())
                        || genderPreference.equalsIgnoreCase(l.getGenderPreference()))
                // 6. Filter by minimum rooms available
                .filter(l -> minRooms == null || l.getRoomsAvailable() >= minRooms)
                .collect(Collectors.toList());
    }

    private boolean hasValidCoordinates(GeoListing geoListing) {
        return geoListing != null
                && geoListing.getLatitude() != null
                && geoListing.getLongitude() != null
                && geoListing.getListingId() != null
                && !geoListing.getListingId().isBlank();
    }

    private com.flatmate.app.listing.Listing safeGetListing(String listingId) {
        try {
            return listingService.getListing(listingId);
        } catch (RuntimeException e) {
            return null;
        }
    }

    // Haversine-based distance calculation (returns km)
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        double theta = lon1 - lon2;
        double dist = Math.sin(Math.toRadians(lat1)) * Math.sin(Math.toRadians(lat2))
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                        * Math.cos(Math.toRadians(theta));
        dist = Math.acos(dist);
        dist = Math.toDegrees(dist);
        dist = dist * 60 * 1.1515 * 1.609344; // convert to km
        return dist;
    }
}
