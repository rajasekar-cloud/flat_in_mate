package com.flatmate.app.location;

import lombok.*;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.*;

@DynamoDbBean
public class GeoListing {

    private String listingId;
    private Double latitude;
    private Double longitude;

    public GeoListing() {
    }

    public GeoListing(String listingId, Double latitude, Double longitude) {
        this.listingId = listingId;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getListingId() { return listingId; }
    public void setListingId(String listingId) { this.listingId = listingId; }
    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }
    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    @DynamoDbPartitionKey
    @DynamoDbAttribute("PK")
    public String getPk() {
        return "GEOLOC#" + listingId;
    }

    public void setPk(String pk) {
    }

    @DynamoDbSortKey
    @DynamoDbAttribute("SK")
    public String getSk() {
        return "COORDINATES";
    }

    public void setSk(String sk) {
    }
}
