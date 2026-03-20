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
public class NotificationRepository {

    private final DynamoDbTable<Notification> notifTable;

    public NotificationRepository(DynamoDbEnhancedClient enhancedClient,
            @Value("${aws.dynamodb.table-name}") String tableName) {
        this.notifTable = enhancedClient.table(tableName, TableSchema.fromBean(Notification.class));
    }

    public void save(Notification notification) {
        notifTable.putItem(notification);
    }

    public List<Notification> findByUserId(String userId) {
        return notifTable.query(QueryConditional.keyEqualTo(Key.builder()
                .partitionValue("USER#" + userId)
                .sortValue("NOTIF#")
                .build()))
                .items()
                .stream()
                .collect(Collectors.toList());
    }
}
