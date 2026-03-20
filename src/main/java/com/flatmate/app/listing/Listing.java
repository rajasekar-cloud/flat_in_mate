package com.flatmate.app.listing;

import lombok.*;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@DynamoDbBean
public class Listing {

    // ── Core identifiers ────────────────────────────────────────────────────
    private String id;
    private String ownerId;
    private String createdAt;
    private String updatedAt;
    private String status; // DRAFT, PUBLISHED, DEACTIVATED

    // ── STEP 1: Place type ───────────────────────────────────────────────────
    // Values: HOUSE, APARTMENT, VILLA, STUDIO
    private String placeType;

    // ── STEP 2: Room type ────────────────────────────────────────────────────
    // Values: PRIVATE_ROOM, SHARED_ROOM
    private String roomType;

    // ── STEP 3: Basics ───────────────────────────────────────────────────────
    private String availableFrom;           // e.g. 2026-04-01

    // Furnishing — Values: UNFURNISHED, SEMI_FURNISHED, FULLY_FURNISHED
    private String furnishingStatus;

    // Floor details
    private Integer totalFloors;
    private String propertyOnFloor;         // GROUND, FIRST, SECOND, THIRD, etc.

    // Floor plan — Values: 1BHK, 2BHK, 3BHK, OTHERS
    private String floorPlan;

    // Floor type — Values: SINGLE, DUPLEX
    private String floorType;

    // Balcony
    private Boolean balconyAvailable;

    // Age of property — Values: 0_1_YEARS, 1_5_YEARS, 5_10_YEARS, 10_PLUS_YEARS
    private String ageOfProperty;

    // Reserved parking
    private Integer carParking;
    private Integer bikeParking;

    // ── STEP 4 & 5: Location (Map pin + Confirm address) ────────────────────
    private Double latitude;
    private Double longitude;
    private Boolean showPreciseLocation;    // true = show exact, false = show area only

    // Address fields
    private String country;                 // Default: India
    private String flatHouseDetails;        // Flat no, house no (e.g. "Flat 4B")
    private String streetAddress;           // e.g. "Akshya Nagar 1st Block 1st Cross"
    private String landmark;                // Nearby landmark
    private String district;               // District/Locality
    private String pinCode;
    private String city;                    // e.g. Bangalore

    // ── STEP 6: Bathroom type ────────────────────────────────────────────────
    // Values: ATTACHED, DEDICATED, SHARED
    private String bathroomType;

    // ── STEP 7: Co-occupants ────────────────────────────────────────────────
    // Values: ONLY_ME, FAMILY, ROOMMATES
    private String occupantType;

    // ── STEP 8: What your place offers ──────────────────────────────────────
    // Counted amenities (each stores the count, e.g. fans = 2)
    private Integer lights;
    private Integer fans;
    private Integer ac;
    private Integer tv;
    private Integer beds;
    private Integer wardrobes;
    private Integer geysers;

    // Toggle amenities (present in list if available)
    // e.g. ["Wifi", "Sofa", "Washing Machine", "Stove", "Water Purifier",
    //        "Microwave", "Dining Table", "Exhaust Fan"]
    private List<String> amenities;

    // Any extra amenities the owner adds manually
    private List<String> customAmenities;

    // Free-text description
    private String description;

    // ── STEP 9: Other features / House rules ────────────────────────────────
    private Boolean petsAllowed;
    private Boolean drinkingAllowed;
    private Boolean smokingAllowed;
    private Boolean powerBackup;

    // ── STEP 10: Photos (minimum 5 required) ────────────────────────────────
    // S3 URLs stored after direct client-side upload
    private List<String> photos;

    // ── STEP 11: Rent ────────────────────────────────────────────────────────
    private Double rent;                    // Selected monthly rent
    private Double minRent;                 // Min of slider range
    private Double maxRent;                 // Max of slider range
    private Double advanceAmount;           // Auto-calculated: 10x monthly rent
    private String noticePeriod;            // NONE, 1_MONTH, 2_MONTHS, 3_MONTHS,
                                            // 4_MONTHS, 5_MONTHS, 6_MONTHS

    // ── Legacy / extra fields ────────────────────────────────────────────────
    private Integer roomsAvailable;
    private String genderPreference;        // MALE, FEMALE, ANY
    private Integer occupancy;             // Max occupants allowed

    // ── DynamoDB single-table keys ───────────────────────────────────────────
    @DynamoDbPartitionKey
    @DynamoDbAttribute("PK")
    public String getPk() {
        return "LISTING#" + id;
    }

    public void setPk(String pk) {
    }

    @DynamoDbSortKey
    @DynamoDbAttribute("SK")
    public String getSk() {
        return "METADATA";
    }

    public void setSk(String sk) {
    }
}
