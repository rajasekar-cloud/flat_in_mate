package com.flatmate.app.notification;

import lombok.*;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@DynamoDbBean
public class Notification {

    private String id;
    private String userId;
    private String title;
    private String message;
    private String timestamp;
    private boolean read;

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
        return "NOTIF#" + id;
    }

    public void setSk(String sk) {
    }
}
