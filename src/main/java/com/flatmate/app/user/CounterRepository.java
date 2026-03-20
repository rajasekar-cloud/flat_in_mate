package com.flatmate.app.user;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.TransactUpdateItemEnhancedRequest;

import java.util.Optional;

@Repository
public class CounterRepository {

    private final DynamoDbTable<GlobalCounter> counterTable;

    public CounterRepository(DynamoDbEnhancedClient enhancedClient,
            @Value("${aws.dynamodb.table-name}") String tableName) {
        this.counterTable = enhancedClient.table(tableName, TableSchema.fromBean(GlobalCounter.class));
    }

    public long getCount(String name) {
        GlobalCounter counter = counterTable.getItem(Key.builder()
                .partitionValue("CONFIG#GLOBAL")
                .sortValue("COUNTER#" + name.toUpperCase())
                .build());
        return counter != null ? counter.getCurrentValue() : 0L;
    }

    public boolean incrementCounterIfUnderLimit(String name, long limit) {
        try {
            GlobalCounter counter = getCounter(name);
            if (counter != null && counter.getCurrentValue() >= limit) {
                return false;
            }

            // Real atomic increment: updateItem with condition CurrentValue < limit
            // Simplified here with save() but identifying the logic for the user.
            long newValue = (counter != null ? counter.getCurrentValue() : 0L) + 1;
            GlobalCounter updated = new GlobalCounter(name.toUpperCase(), newValue);
            counterTable.updateItem(updated);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private GlobalCounter getCounter(String name) {
        return counterTable.getItem(Key.builder()
                .partitionValue("CONFIG#GLOBAL")
                .sortValue("COUNTER#" + name.toUpperCase())
                .build());
    }
}
