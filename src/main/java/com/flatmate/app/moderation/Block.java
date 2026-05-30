package com.flatmate.app.moderation;

import lombok.*;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.*;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@DynamoDbBean
public class Block {
    private String blockerId;
    private String blockedId;
    private String createdAt;

    public String getBlockerId() { return blockerId; }
    public void setBlockerId(String blockerId) { this.blockerId = blockerId; }

    public String getBlockedId() { return blockedId; }
    public void setBlockedId(String blockedId) { this.blockedId = blockedId; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    @DynamoDbPartitionKey
    @DynamoDbAttribute("PK")
    public String getPk() {
        return "BLOCK#" + blockerId;
    }

    public void setPk(String pk) {}

    @DynamoDbSortKey
    @DynamoDbAttribute("SK")
    public String getSk() {
        return "BLOCKED#" + blockedId;
    }

    public void setSk(String sk) {}
}
