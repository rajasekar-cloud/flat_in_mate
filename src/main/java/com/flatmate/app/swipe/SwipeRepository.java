package com.flatmate.app.swipe;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;

@Repository
public class SwipeRepository {

    private final DynamoDbTable<Swipe> swipeTable;

    public SwipeRepository(DynamoDbEnhancedClient enhancedClient,
            @Value("${aws.dynamodb.table-name}") String tableName) {
        this.swipeTable = enhancedClient.table(tableName, TableSchema.fromBean(Swipe.class));
    }

    public void save(Swipe swipe) {
        swipeTable.putItem(swipe);
    }

    public long countBySeekerId(String seekerId) {
        return swipeTable.query(QueryConditional.sortBeginsWith(Key.builder()
                .partitionValue("USER#" + seekerId)
                .sortValue("SWIPE#")
                .build()))
                .stream()
                .flatMap(p -> p.items().stream())
                .count();
    }
}
