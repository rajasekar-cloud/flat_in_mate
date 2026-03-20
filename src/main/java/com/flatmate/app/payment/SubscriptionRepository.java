package com.flatmate.app.payment;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;

import java.util.Optional;

@Repository
public class SubscriptionRepository {

    private final DynamoDbTable<Subscription> subTable;

    public SubscriptionRepository(DynamoDbEnhancedClient enhancedClient,
            @Value("${aws.dynamodb.table-name}") String tableName) {
        this.subTable = enhancedClient.table(tableName, TableSchema.fromBean(Subscription.class));
    }

    public void save(Subscription sub) {
        subTable.putItem(sub);
    }

    public Optional<Subscription> findByUserId(String userId) {
        return subTable.query(QueryConditional.sortBeginsWith(Key.builder()
                .partitionValue("USER#" + userId)
                .sortValue("SUB#")
                .build()))
                .stream()
                .flatMap(p -> p.items().stream())
                .findFirst();
    }
}
