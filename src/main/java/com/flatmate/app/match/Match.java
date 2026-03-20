package com.flatmate.app.match;

import lombok.*;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@DynamoDbBean
public class Match {

    private String matchId;
    private String seekerId;
    private String ownerId;
    private String listingId;
    private String status; // PENDING, APPROVED, REJECTED
    private String createdAt;
    private String updatedAt;
    private String seekerLastRead;
    private String ownerLastRead;

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
        return "MATCH#" + ownerId;
    }

    public void setSk(String sk) {
    }
}
