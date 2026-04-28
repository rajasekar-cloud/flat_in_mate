package com.flatmate.app.user;

import lombok.*;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.*;

@DynamoDbBean
public class GlobalCounter {

    private String counterName;
    private Long currentValue;

    public GlobalCounter() {
    }

    public GlobalCounter(String counterName, Long currentValue) {
        this.counterName = counterName;
        this.currentValue = currentValue;
    }

    public String getCounterName() { return counterName; }
    public void setCounterName(String counterName) { this.counterName = counterName; }
    public Long getCurrentValue() { return currentValue; }
    public void setCurrentValue(Long currentValue) { this.currentValue = currentValue; }

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
        return "COUNTER#" + (counterName != null ? counterName.toUpperCase() : "UNKNOWN");
    }

    public void setSk(String sk) {
    }
}
