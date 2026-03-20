package com.flatmate.app.auth;

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
public class RoleChangeHistoryRepository {

    private final DynamoDbTable<RoleChangeHistory> table;

    public RoleChangeHistoryRepository(DynamoDbEnhancedClient enhancedClient,
            @Value("${aws.dynamodb.table-name}") String tableName) {
        this.table = enhancedClient.table(tableName, TableSchema.fromBean(RoleChangeHistory.class));
    }

    public void save(RoleChangeHistory history) {
        table.putItem(history);
    }

    public List<RoleChangeHistory> findByUserId(String userId) {
        return table.query(QueryConditional.sortBeginsWith(Key.builder()
                .partitionValue("USER#" + userId)
                .sortValue("ROLE_CHANGE#")
                .build()))
                .stream()
                .flatMap(page -> page.items().stream())
                .collect(Collectors.toList());
    }
}
