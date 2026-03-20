package com.flatmate.app.payment;

import lombok.*;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@DynamoDbBean
public class Subscription {

    private String userId;
    private String planId;
    private String razorpayOrderId;
    private String status; // ACTIVE, EXPIRED
    private String expiresAt;

    @DynamoDbPartitionKey
    @DynamoDbAttribute("PK")
    public String getPk() {
        return "USER#" + userId;
    }

    public void setPk(String pk) {
    }

    @DynamoDbSortKey
    @DynamoDbAttribute("SK")
    public String getSk() {
        return "SUB#" + planId;
    }

    public void setSk(String sk) {
    }
}
