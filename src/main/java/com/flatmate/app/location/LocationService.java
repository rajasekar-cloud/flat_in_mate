package com.flatmate.app.location;

import com.flatmate.app.auth.User;
import com.flatmate.app.auth.UserRepository;
import com.flatmate.app.listing.Listing;
import com.flatmate.app.listing.ListingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LocationService {

        private final GeoListingRepository geoListingRepository;
        private final ListingService listingService;
        private final UserRepository userRepository;

        // ✅ Safe insert (prevents bad DB data)
        public void indexListing(String listingId, Double lat, Double lng) {

                if (listingId == null || lat == null || lng == null) {
                        throw new IllegalArgumentException("Invalid geo data: listingId/lat/lng cannot be null");
                }

                GeoListing geo = GeoListing.builder()
                                .listingId(listingId)
                                .latitude(lat)
                                .longitude(lng)
                                .build();

                geoListingRepository.save(geo);
        }

        public List<NearbyListingResponse> findNearbyListings(
                        double lat, double lng, double radiusKm,
                        Double minRent, Double maxRent,
                        String genderPreference, Integer minRooms) {

                return geoListingRepository.findAll().stream()

                                // ✅ 1. Remove invalid geo records (fix for NPE)
                                .filter(g -> {
                                        if (g.getLatitude() == null || g.getLongitude() == null) {
                                                System.out.println("⚠️ Skipping GeoListing with NULL lat/lng. GeoId: "
                                                                + g.getPk());
                                                return false;
                                        }
                                        return true;
                                })

                                // ✅ 2. Distance filter (safe now)
                                .filter(g -> calculateDistance(lat, lng, g.getLatitude(), g.getLongitude()) <= radiusKm)

                                // ✅ 3. Remove invalid listingId
                                .filter(g -> {
                                        if (g.getListingId() == null) {
                                                System.out.println("⚠️ Skipping GeoListing with NULL listingId. GeoId: "
                                                                + g.getPk());
                                                return false;
                                        }
                                        return true;
                                })

                                // ✅ 4. Fetch listing safely
                                .map(g -> {
                                        try {
                                                return listingService.getListing(g.getListingId());
                                        } catch (Exception e) {
                                                System.out.println("❌ Failed to fetch listing for ID: "
                                                                + g.getListingId());
                                                return null;
                                        }
                                })

                                // ✅ 5. Remove failed fetch
                                .filter(Objects::nonNull)

                                // ✅ 6. Apply filters
                                .filter(l -> minRent == null || l.getRent() >= minRent)
                                .filter(l -> maxRent == null || l.getRent() <= maxRent)
                                .filter(l -> genderPreference == null
                                                || "ANY".equalsIgnoreCase(l.getGenderPreference())
                                                || genderPreference.equalsIgnoreCase(l.getGenderPreference()))
                                .filter(l -> minRooms == null || l.getRoomsAvailable() >= minRooms)

                                // ✅ 7. Build response
                                .map(l -> NearbyListingResponse.builder()
                                                .listing(l)
                                                .ownerUsername(resolveOwnerUsername(l.getOwnerId()))
                                                .build())

                                .collect(Collectors.toList());
        }

        /** Resolve owner display name safely */
        private String resolveOwnerUsername(String ownerId) {
                if (ownerId == null)
                        return "Unknown";

                return userRepository.findById(ownerId)
                                .map(this::buildDisplayName)
                                .orElse(ownerId);
        }

        private String buildDisplayName(User user) {
                String first = user.getFirstName();
                String last = user.getLastName();

                if (first != null && last != null)
                        return (first + " " + last).trim();
                if (first != null)
                        return first;
                if (last != null)
                        return last;
                if (user.getName() != null)
                        return user.getName();

                return user.getUserId();
        }

        // Haversine distance (km)
        private double calculateDistance(double lat1, double lon1, Double lat2, Double lon2) {

                // Extra safety (never trust DB)
                if (lat2 == null || lon2 == null) {
                        return Double.MAX_VALUE;
                }

                double theta = lon1 - lon2;

                double dist = Math.sin(Math.toRadians(lat1)) * Math.sin(Math.toRadians(lat2))
                                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                                                * Math.cos(Math.toRadians(theta));

                dist = Math.acos(dist);
                dist = Math.toDegrees(dist);
                dist = dist * 60 * 1.1515 * 1.609344;

                return dist;
        }
}