package com.flatmate.app.listing;

import lombok.*;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.*;

import java.util.List;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@DynamoDbBean
public class Listing {

    private String id;
    private String ownerId;
    private String propertyName;
    private String createdAt;
    private String updatedAt;
    private String status;
    private String placeType;
    private String roomType;
    private String availableFrom;
    private String furnishingStatus;
    private Integer totalFloors;
    private String propertyOnFloor;
    private String floorPlan;
    private String floorType;
    private Boolean balconyAvailable;
    private String ageOfProperty;
    private Integer carParking;
    private Integer bikeParking;
    private Double latitude;
    private Double longitude;
    private Boolean showPreciseLocation;
    private String country;
    private String flatHouseDetails;
    private String streetAddress;
    private String landmark;
    private String district;
    private String pinCode;
    private String city;
    private String bathroomType;
    private String occupantType;
    private Integer lights;
    private Integer fans;
    private Integer ac;
    private Integer tv;
    private Integer beds;
    private Integer wardrobes;
    private Integer geysers;
    private List<String> amenities;
    private List<String> customAmenities;
    private String description;
    private Boolean petsAllowed;
    private Boolean drinkingAllowed;
    private Boolean smokingAllowed;
    private Boolean powerBackup;
    private List<String> photos;
    private Double rent;
    private Double minRent;
    private Double maxRent;
    private Double advanceAmount;
    private String noticePeriod;
    private Integer roomsAvailable;
    private String genderPreference;
    private Integer occupancy;

    // --- Getters ---
    public String getId() { return id; }
    public String getOwnerId() { return ownerId; }
    public String getPropertyName() { return propertyName; }
    public String getCreatedAt() { return createdAt; }
    public String getUpdatedAt() { return updatedAt; }
    public String getStatus() { return status; }
    public String getPlaceType() { return placeType; }
    public String getRoomType() { return roomType; }
    public String getAvailableFrom() { return availableFrom; }
    public String getFurnishingStatus() { return furnishingStatus; }
    public Integer getTotalFloors() { return totalFloors; }
    public String getPropertyOnFloor() { return propertyOnFloor; }
    public String getFloorPlan() { return floorPlan; }
    public String getFloorType() { return floorType; }
    public Boolean getBalconyAvailable() { return balconyAvailable; }
    public String getAgeOfProperty() { return ageOfProperty; }
    public Integer getCarParking() { return carParking; }
    public Integer getBikeParking() { return bikeParking; }
    public Double getLatitude() { return latitude; }
    public Double getLongitude() { return longitude; }
    public Boolean getShowPreciseLocation() { return showPreciseLocation; }
    public String getCountry() { return country; }
    public String getFlatHouseDetails() { return flatHouseDetails; }
    public String getStreetAddress() { return streetAddress; }
    public String getLandmark() { return landmark; }
    public String getDistrict() { return district; }
    public String getPinCode() { return pinCode; }
    public String getCity() { return city; }
    public String getBathroomType() { return bathroomType; }
    public String getOccupantType() { return occupantType; }
    public Integer getLights() { return lights; }
    public Integer getFans() { return fans; }
    public Integer getAc() { return ac; }
    public Integer getTv() { return tv; }
    public Integer getBeds() { return beds; }
    public Integer getWardrobes() { return wardrobes; }
    public Integer getGeysers() { return geysers; }
    public List<String> getAmenities() { return amenities; }
    public List<String> getCustomAmenities() { return customAmenities; }
    public String getDescription() { return description; }
    public Boolean getPetsAllowed() { return petsAllowed; }
    public Boolean getDrinkingAllowed() { return drinkingAllowed; }
    public Boolean getSmokingAllowed() { return smokingAllowed; }
    public Boolean getPowerBackup() { return powerBackup; }
    public List<String> getPhotos() { return photos; }
    public Double getRent() { return rent; }
    public Double getMinRent() { return minRent; }
    public Double getMaxRent() { return maxRent; }
    public Double getAdvanceAmount() { return advanceAmount; }
    public String getNoticePeriod() { return noticePeriod; }
    public Integer getRoomsAvailable() { return roomsAvailable; }
    public String getGenderPreference() { return genderPreference; }
    public Integer getOccupancy() { return occupancy; }

    // --- Setters ---
    public void setId(String id) { this.id = id; }
    public void setOwnerId(String ownerId) { this.ownerId = ownerId; }
    public void setPropertyName(String propertyName) { this.propertyName = propertyName; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
    public void setStatus(String status) { this.status = status; }
    public void setPlaceType(String placeType) { this.placeType = placeType; }
    public void setRoomType(String roomType) { this.roomType = roomType; }
    public void setAvailableFrom(String availableFrom) { this.availableFrom = availableFrom; }
    public void setFurnishingStatus(String furnishingStatus) { this.furnishingStatus = furnishingStatus; }
    public void setTotalFloors(Integer totalFloors) { this.totalFloors = totalFloors; }
    public void setPropertyOnFloor(String propertyOnFloor) { this.propertyOnFloor = propertyOnFloor; }
    public void setFloorPlan(String floorPlan) { this.floorPlan = floorPlan; }
    public void setFloorType(String floorType) { this.floorType = floorType; }
    public void setBalconyAvailable(Boolean balconyAvailable) { this.balconyAvailable = balconyAvailable; }
    public void setAgeOfProperty(String ageOfProperty) { this.ageOfProperty = ageOfProperty; }
    public void setCarParking(Integer carParking) { this.carParking = carParking; }
    public void setBikeParking(Integer bikeParking) { this.bikeParking = bikeParking; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
    public void setShowPreciseLocation(Boolean showPreciseLocation) { this.showPreciseLocation = showPreciseLocation; }
    public void setCountry(String country) { this.country = country; }
    public void setFlatHouseDetails(String flatHouseDetails) { this.flatHouseDetails = flatHouseDetails; }
    public void setStreetAddress(String streetAddress) { this.streetAddress = streetAddress; }
    public void setLandmark(String landmark) { this.landmark = landmark; }
    public void setDistrict(String district) { this.district = district; }
    public void setPinCode(String pinCode) { this.pinCode = pinCode; }
    public void setCity(String city) { this.city = city; }
    public void setBathroomType(String bathroomType) { this.bathroomType = bathroomType; }
    public void setOccupantType(String occupantType) { this.occupantType = occupantType; }
    public void setLights(Integer lights) { this.lights = lights; }
    public void setFans(Integer fans) { this.fans = fans; }
    public void setAc(Integer ac) { this.ac = ac; }
    public void setTv(Integer tv) { this.tv = tv; }
    public void setBeds(Integer beds) { this.beds = beds; }
    public void setWardrobes(Integer wardrobes) { this.wardrobes = wardrobes; }
    public void setGeysers(Integer geysers) { this.geysers = geysers; }
    public void setAmenities(List<String> amenities) { this.amenities = amenities; }
    public void setCustomAmenities(List<String> customAmenities) { this.customAmenities = customAmenities; }
    public void setDescription(String description) { this.description = description; }
    public void setPetsAllowed(Boolean petsAllowed) { this.petsAllowed = petsAllowed; }
    public void setDrinkingAllowed(Boolean drinkingAllowed) { this.drinkingAllowed = drinkingAllowed; }
    public void setSmokingAllowed(Boolean smokingAllowed) { this.smokingAllowed = smokingAllowed; }
    public void setPowerBackup(Boolean powerBackup) { this.powerBackup = powerBackup; }
    public void setPhotos(List<String> photos) { this.photos = photos; }
    public void setRent(Double rent) { this.rent = rent; }
    public void setMinRent(Double minRent) { this.minRent = minRent; }
    public void setMaxRent(Double maxRent) { this.maxRent = maxRent; }
    public void setAdvanceAmount(Double advanceAmount) { this.advanceAmount = advanceAmount; }
    public void setNoticePeriod(String noticePeriod) { this.noticePeriod = noticePeriod; }
    public void setRoomsAvailable(Integer roomsAvailable) { this.roomsAvailable = roomsAvailable; }
    public void setGenderPreference(String genderPreference) { this.genderPreference = genderPreference; }
    public void setOccupancy(Integer occupancy) { this.occupancy = occupancy; }

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
