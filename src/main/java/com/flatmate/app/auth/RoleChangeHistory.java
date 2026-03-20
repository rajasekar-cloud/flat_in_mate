package com.flatmate.app.auth;

import lombok.*;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@DynamoDbBean
public class RoleChangeHistory {

    private String userId;
    private String fromRole;
    private String toRole;
    private String changedAt; // ISO timestamp, e.g. 2026-03-10T12:00:00

    @DynamoDbPartitionKey
    @DynamoDbAttribute("PK")
    public String getPk() {
        return "USER#" + userId;
    }

    public void setPk(String pk) {
        // Required for DynamoDB Mapper
    }

    @DynamoDbSortKey
    @DynamoDbAttribute("SK")
    public String getSk() {
        return "ROLE_CHANGE#" + changedAt;
    }

    public void setSk(String sk) {
        // Required for DynamoDB Mapper
    }
}
