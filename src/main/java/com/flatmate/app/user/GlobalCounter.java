package com.flatmate.app.user;

import lombok.*;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@DynamoDbBean
public class GlobalCounter {

    private String counterName;
    private Long currentValue;

    @DynamoDbPartitionKey
    @DynamoDbAttribute("PK")
    public String getPk() {
        return "CONFIG#GLOBAL";
    }

    public void setPk(String pk) {
    }

    @DynamoDbSortKey
    @DynamoDbAttribute("SK")
    public String getSk() {
        return "COUNTER#" + counterName.toUpperCase();
    }

    public void setSk(String sk) {
    }
}
