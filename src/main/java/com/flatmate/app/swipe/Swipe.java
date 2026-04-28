package com.flatmate.app.swipe;

import lombok.*;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.*;

@NoArgsConstructor
@AllArgsConstructor
@DynamoDbBean
public class Swipe {

    private String seekerId;
    private String listingId;
    private String type; // RIGHT, LEFT
    private String createdAt;

    public String getSeekerId() { return seekerId; }
    public void setSeekerId(String seekerId) { this.seekerId = seekerId; }

    @DynamoDbSecondaryPartitionKey(indexNames = "ListingIndex")
    public String getListingId() { return listingId; }
    public void setListingId(String listingId) { this.listingId = listingId; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    @DynamoDbPartitionKey
    @DynamoDbAttribute("PK")
    public String getPk() {
        return "USER#" + seekerId;
    }

    public void setPk(String pk) {
    }

    @DynamoDbSortKey
    @DynamoDbAttribute("SK")
    public String getSk() {
        return "SWIPE#" + listingId;
    }

    public void setSk(String sk) {
    }
}
