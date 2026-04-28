package com.flatmate.app.location;

import com.flatmate.app.auth.User;
import com.flatmate.app.auth.UserRepository;
import com.flatmate.app.listing.Listing;
import com.flatmate.app.listing.ListingService;
import com.flatmate.app.swipe.Swipe;
import com.flatmate.app.swipe.SwipeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.geo.Circle;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.Metrics;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LocationService {

        private static final Logger log = LoggerFactory.getLogger(LocationService.class);

        private final GeoListingRepository geoListingRepository;
        private final ListingService listingService;
        private final UserRepository userRepository;
        private final SwipeRepository swipeRepository;
        private final StringRedisTemplate redisTemplate;

        private static final String GEO_KEY = "geo:listings";

        // ✅ Index listing (DB + Redis)
        public void indexListing(String listingId, Double lat, Double lng) {

                if (listingId == null || lat == null || lng == null) {
                        throw new IllegalArgumentException("Invalid geo data");
                }

                // Save in DB
                GeoListing geo = new GeoListing();
                geo.setListingId(listingId);
                geo.setLatitude(lat);
                geo.setLongitude(lng);

                geoListingRepository.save(geo);

                // 🔥 Save in Redis GEO
                redisTemplate.opsForGeo().add(
                                GEO_KEY,
                                new Point(lng, lat),
                                listingId);
        }

        // ✅ Main optimized method
        public List<NearbyListingResponse> findNearbyListings(
                        String currentUserId,
                        double lat, double lng, double radiusKm,
                        Double minRent, Double maxRent,
                        String genderPreference, Integer minRooms) {

                // ✅ Get swiped listings
                final Set<String> swipedListingIds = (currentUserId != null && !currentUserId.isEmpty())
                                ? swipeRepository.findBySeekerId(currentUserId)
                                                .stream()
                                                .map(Swipe::getListingId)
                                                .collect(Collectors.toSet())
                                : new HashSet<>();

                // ✅ Redis GEO search
                Circle circle = new Circle(
                                new Point(lng, lat),
                                new Distance(radiusKm, Metrics.KILOMETERS));

                List<String> nearbyListingIds = redisTemplate.opsForGeo()
                                .radius(GEO_KEY, circle,
                                                RedisGeoCommands.GeoRadiusCommandArgs.newGeoRadiusArgs()
                                                                .includeDistance()
                                                                .sortAscending())
                                .getContent()
                                .stream()
                                .map(r -> r.getContent().getName())
                                .collect(Collectors.toList());

                // ✅ Fetch listings + apply filters
                return nearbyListingIds.stream()

                                .map(id -> {
                                        try {
                                                return listingService.getListing(id);
                                        } catch (Exception e) {
                                                log.error("❌ Failed to fetch listing: {}", id);
                                                return null;
                                        }
                                })

                                .filter(Objects::nonNull)

                                // 🔥 Business filters
                                .filter(l -> currentUserId == null || currentUserId.isEmpty()
                                                || !swipedListingIds.contains(l.getId()))

                                .filter(l -> currentUserId == null || currentUserId.isEmpty()
                                                || !currentUserId.equals(l.getOwnerId()))

                                .filter(l -> "PUBLISHED".equalsIgnoreCase(l.getStatus()))

                                .filter(l -> minRent == null || l.getRent() >= minRent)
                                .filter(l -> maxRent == null || l.getRent() <= maxRent)

                                .filter(l -> genderPreference == null
                                                || "ANY".equalsIgnoreCase(l.getGenderPreference())
                                                || genderPreference.equalsIgnoreCase(l.getGenderPreference()))

                                .filter(l -> minRooms == null || l.getRoomsAvailable() >= minRooms)

                                // ✅ Response mapping
                                .map(l -> {
                                        NearbyListingResponse resp = new NearbyListingResponse();
                                        resp.setListing(l);
                                        resp.setOwnerUsername(resolveOwnerUsername(l.getOwnerId()));
                                        return resp;
                                })

                                .collect(Collectors.toList());
        }

        // ✅ Resolve owner name
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

        // ✅ Optional: backfill Redis (run once)
        public void backfillRedis() {
                geoListingRepository.findAll().forEach(g -> {
                        if (g.getLatitude() != null && g.getLongitude() != null && g.getListingId() != null) {
                                redisTemplate.opsForGeo().add(
                                                GEO_KEY,
                                                new Point(g.getLongitude(), g.getLatitude()),
                                                g.getListingId());
                        }
                });
        }

        // ✅ Optional: remove from Redis
        public void removeListing(String listingId) {
                redisTemplate.opsForGeo().remove(GEO_KEY, listingId);
        }
}