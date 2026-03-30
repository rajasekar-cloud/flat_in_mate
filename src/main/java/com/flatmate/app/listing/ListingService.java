package com.flatmate.app.listing;

import com.flatmate.app.auth.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;
import org.springframework.beans.factory.annotation.Value;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ListingService {

    private final ListingRepository listingRepository;
    private final UserRepository userRepository;
    private final S3Presigner s3Presigner;

    @Value("${aws.s3.bucket}")
    private String bucketName;

    /**
     * Create a new listing (owner onboarding — Step 1 to 11).
     * Auto-calculates advanceAmount = 10x monthly rent.
     * Sets ownerOnboardingComplete = true on the owner's User record.
     * Validates that at least 5 photos are attached (Step 10 requirement).
     */
    public Listing createListing(Listing listing) {
        if (listing.getId() == null) {
            listing.setId(UUID.randomUUID().toString());
        }
        listing.setCreatedAt(LocalDateTime.now().toString());
        listing.setStatus("PUBLISHED");

        // Validate minimum 5 photos required
        if (listing.getPhotos() == null || listing.getPhotos().size() < 5) {
            throw new RuntimeException("Minimum 5 photos are required to publish a listing.");
        }

        // Auto-calculate advance = 10x monthly rent
        if (listing.getRent() != null) {
            listing.setAdvanceAmount(listing.getRent() * 10);
        }

        Listing saved = listingRepository.save(listing);

        // Mark owner onboarding as complete once their first listing is created
        if (listing.getOwnerId() != null) {
            userRepository.findById(listing.getOwnerId()).ifPresent(user -> {
                user.setOwnerOnboardingComplete(true);
                userRepository.save(user);
            });
        }

        return saved;
    }

    /**
     * Save listing as DRAFT (owner hasn't finished all steps yet).
     * No photo count validation — allows partial save.
     */
    public Listing saveDraft(Listing listing) {
        if (listing.getId() == null) {
            listing.setId(UUID.randomUUID().toString());
        }
        listing.setCreatedAt(LocalDateTime.now().toString());
        listing.setStatus("DRAFT");
        if (listing.getRent() != null) {
            listing.setAdvanceAmount(listing.getRent() * 10);
        }
        return listingRepository.save(listing);
    }

    /**
     * Update an existing listing (owner edits after publish).
     */
    public Listing updateListing(String id, Listing updates) {
        Listing existing = getListing(id);

        // Basic Info
        if (updates.getPropertyName() != null) existing.setPropertyName(updates.getPropertyName());
        if (updates.getPlaceType() != null) existing.setPlaceType(updates.getPlaceType());
        if (updates.getRoomType() != null) existing.setRoomType(updates.getRoomType());
        if (updates.getAvailableFrom() != null) existing.setAvailableFrom(updates.getAvailableFrom());
        if (updates.getFurnishingStatus() != null) existing.setFurnishingStatus(updates.getFurnishingStatus());
        if (updates.getFloorPlan() != null) existing.setFloorPlan(updates.getFloorPlan());
        if (updates.getFloorType() != null) existing.setFloorType(updates.getFloorType());
        if (updates.getPropertyOnFloor() != null) existing.setPropertyOnFloor(updates.getPropertyOnFloor());
        if (updates.getTotalFloors() != null) existing.setTotalFloors(updates.getTotalFloors());
        if (updates.getAgeOfProperty() != null) existing.setAgeOfProperty(updates.getAgeOfProperty());
        if (updates.getBalconyAvailable() != null) existing.setBalconyAvailable(updates.getBalconyAvailable());
        
        // Parking
        if (updates.getCarParking() != null) existing.setCarParking(updates.getCarParking());
        if (updates.getBikeParking() != null) existing.setBikeParking(updates.getBikeParking());

        // Location
        if (updates.getLatitude() != null) existing.setLatitude(updates.getLatitude());
        if (updates.getLongitude() != null) existing.setLongitude(updates.getLongitude());
        if (updates.getShowPreciseLocation() != null) existing.setShowPreciseLocation(updates.getShowPreciseLocation());
        if (updates.getFlatHouseDetails() != null) existing.setFlatHouseDetails(updates.getFlatHouseDetails());
        if (updates.getStreetAddress() != null) existing.setStreetAddress(updates.getStreetAddress());
        if (updates.getLandmark() != null) existing.setLandmark(updates.getLandmark());
        if (updates.getDistrict() != null) existing.setDistrict(updates.getDistrict());
        if (updates.getPinCode() != null) existing.setPinCode(updates.getPinCode());
        if (updates.getCity() != null) existing.setCity(updates.getCity());

        // Features & Counts
        if (updates.getBathroomType() != null) existing.setBathroomType(updates.getBathroomType());
        if (updates.getOccupantType() != null) existing.setOccupantType(updates.getOccupantType());
        if (updates.getLights() != null) existing.setLights(updates.getLights());
        if (updates.getFans() != null) existing.setFans(updates.getFans());
        if (updates.getAc() != null) existing.setAc(updates.getAc());
        if (updates.getTv() != null) existing.setTv(updates.getTv());
        if (updates.getBeds() != null) existing.setBeds(updates.getBeds());
        if (updates.getWardrobes() != null) existing.setWardrobes(updates.getWardrobes());
        if (updates.getGeysers() != null) existing.setGeysers(updates.getGeysers());
        
        // Amenities lists
        if (updates.getAmenities() != null) existing.setAmenities(updates.getAmenities());
        if (updates.getCustomAmenities() != null) existing.setCustomAmenities(updates.getCustomAmenities());
        
        // Rules
        if (updates.getPetsAllowed() != null) existing.setPetsAllowed(updates.getPetsAllowed());
        if (updates.getDrinkingAllowed() != null) existing.setDrinkingAllowed(updates.getDrinkingAllowed());
        if (updates.getSmokingAllowed() != null) existing.setSmokingAllowed(updates.getSmokingAllowed());
        if (updates.getPowerBackup() != null) existing.setPowerBackup(updates.getPowerBackup());

        // Pricing & Policy
        if (updates.getRent() != null) {
            existing.setRent(updates.getRent());
            existing.setAdvanceAmount(updates.getRent() * 10);
        }
        if (updates.getMinRent() != null) existing.setMinRent(updates.getMinRent());
        if (updates.getMaxRent() != null) existing.setMaxRent(updates.getMaxRent());
        if (updates.getNoticePeriod() != null) existing.setNoticePeriod(updates.getNoticePeriod());

        // Photos
        if (updates.getPhotos() != null) existing.setPhotos(updates.getPhotos());

        // Meta
        if (updates.getDescription() != null) existing.setDescription(updates.getDescription());
        if (updates.getStatus() != null) existing.setStatus(updates.getStatus());
        
        existing.setUpdatedAt(LocalDateTime.now().toString());
        return listingRepository.save(existing);
    }

    /**
     * Deactivate a listing (soft delete — changes status to DEACTIVATED).
     */
    public void deactivateListing(String id) {
        Listing listing = getListing(id);
        listing.setStatus("DEACTIVATED");
        listing.setUpdatedAt(LocalDateTime.now().toString());
        listingRepository.save(listing);
    }

    public List<Listing> getAllListings() {
        return listingRepository.findAll();
    }

    public Listing getListing(String id) {
        return listingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Listing not found: " + id));
    }

    public List<Listing> getListingsByOwner(String ownerId) {
        return listingRepository.findAll().stream()
                .filter(l -> ownerId.equals(l.getOwnerId()))
                .toList();
    }

    /**
     * Generate a pre-signed S3 URL for direct client-side file upload.
     * Supports: JPEG, PNG, HEIC (iOS), MP4, MOV (video tours), GIF, WebP.
     * URL expires in 15 minutes.
     */
    public String generateUploadUrl(String key) {
        String contentType = detectContentType(key);

        PutObjectRequest objectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .contentType(contentType)
                .build();

        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(15))
                .putObjectRequest(objectRequest)
                .build();

        return s3Presigner.presignPutObject(presignRequest).url().toString();
    }

    private String detectContentType(String key) {
        String lower = key.toLowerCase();
        if (lower.endsWith(".png"))  return "image/png";
        if (lower.endsWith(".heic")) return "image/heic";
        if (lower.endsWith(".heif")) return "image/heif";
        if (lower.endsWith(".gif"))  return "image/gif";
        if (lower.endsWith(".webp")) return "image/webp";
        if (lower.endsWith(".mp4"))  return "video/mp4";
        if (lower.endsWith(".mov"))  return "video/quicktime";
        if (lower.endsWith(".avi"))  return "video/x-msvideo";
        return "image/jpeg";
    }
}
