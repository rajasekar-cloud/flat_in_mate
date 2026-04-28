package com.flatmate.app.match;

import lombok.*;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.*;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@DynamoDbBean
public class Match {

    private String matchId;
    private String seekerId;
    private String ownerId;
    private String listingId;
    private String status;
    private String createdAt;
    private String updatedAt;
    private String seekerLastRead;
    private String ownerLastRead;

    public String getMatchId() { return matchId; }
    public void setMatchId(String matchId) { this.matchId = matchId; }
    public String getSeekerId() { return seekerId; }
    public void setSeekerId(String seekerId) { this.seekerId = seekerId; }
    public String getOwnerId() { return ownerId; }
    public void setOwnerId(String ownerId) { this.ownerId = ownerId; }
    public String getListingId() { return listingId; }
    public void setListingId(String listingId) { this.listingId = listingId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
    public String getSeekerLastRead() { return seekerLastRead; }
    public void setSeekerLastRead(String seekerLastRead) { this.seekerLastRead = seekerLastRead; }
    public String getOwnerLastRead() { return ownerLastRead; }
    public void setOwnerLastRead(String ownerLastRead) { this.ownerLastRead = ownerLastRead; }

    @DynamoDbPartitionKey
    @DynamoDbAttribute("PK")
    public String getPk() {
        return "MATCH#" + matchId;
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
