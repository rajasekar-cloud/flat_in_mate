package com.flatmate.app.notification;

import lombok.*;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@DynamoDbBean
public class DeviceToken {

    private String userId;
    private String fcmToken;
    private String platform; // IOS, ANDROID

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
        return "TOKEN#" + fcmToken;
    }

    public void setSk(String sk) {
    }
}
