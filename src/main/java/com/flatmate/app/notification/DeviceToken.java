package com.flatmate.app.notification;

import lombok.*;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.*;

@NoArgsConstructor
@AllArgsConstructor
@DynamoDbBean
public class DeviceToken {

    private String userId;
    private String fcmToken;
    private String platform; // IOS, ANDROID

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getFcmToken() { return fcmToken; }
    public void setFcmToken(String fcmToken) { this.fcmToken = fcmToken; }
    public String getPlatform() { return platform; }
    public void setPlatform(String platform) { this.platform = platform; }

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
