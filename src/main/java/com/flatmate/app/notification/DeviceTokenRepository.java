package com.flatmate.app.notification;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;

import java.util.List;
import java.util.stream.Collectors;

@Repository
public class DeviceTokenRepository {

    private final DynamoDbTable<DeviceToken> tokenTable;

    public DeviceTokenRepository(DynamoDbEnhancedClient enhancedClient,
            @Value("${aws.dynamodb.table-name}") String tableName) {
        this.tokenTable = enhancedClient.table(tableName, TableSchema.fromBean(DeviceToken.class));
    }

    public void save(DeviceToken token) {
        tokenTable.putItem(token);
    }

    public List<DeviceToken> findByUserId(String userId) {
        return tokenTable.query(QueryConditional.sortBeginsWith(Key.builder()
                .partitionValue("USER#" + userId)
                .sortValue("TOKEN#")
                .build()))
                .stream()
                .flatMap(p -> p.items().stream())
                .collect(Collectors.toList());
    }
}
