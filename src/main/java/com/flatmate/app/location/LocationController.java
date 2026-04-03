package com.flatmate.app.location;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/location")
@RequiredArgsConstructor
public class LocationController {

    private final LocationService locationService;

    // Index a listing's geo-coordinates after creation
    @PostMapping("/index")
    public ResponseEntity<String> indexListing(@RequestBody GeoListing geoListing) {
        locationService.indexListing(geoListing.getListingId(), geoListing.getLatitude(), geoListing.getLongitude());
        return ResponseEntity.ok("Listing indexed successfully");
    }

    // Find listings near a location — supports optional filters
    // Default flow: seeker sends current GPS → gets nearby flats
    // Filter flow: seeker changes location in filter → sends new lat/lng
    @GetMapping("/nearby")
    public ResponseEntity<List<NearbyListingResponse>> getNearbyListings(
            @RequestParam Double lat,
            @RequestParam Double lng,
            @RequestParam(defaultValue = "10.0") Double radius,
            @RequestParam(required = false) Double minRent,
            @RequestParam(required = false) Double maxRent,
            @RequestParam(required = false) String genderPreference,
            @RequestParam(required = false) Integer minRooms) {

        return ResponseEntity.ok(locationService.findNearbyListings(
                lat, lng, radius, minRent, maxRent, genderPreference, minRooms));
    }
}
