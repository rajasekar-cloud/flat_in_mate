package com.flatmate.app.chat;

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
public class MessageRepository {

    private final DynamoDbTable<Message> messageTable;

    public MessageRepository(DynamoDbEnhancedClient enhancedClient,
            @Value("${aws.dynamodb.table-name}") String tableName) {
        this.messageTable = enhancedClient.table(tableName, TableSchema.fromBean(Message.class));
    }

    public Message save(Message message) {
        messageTable.putItem(message);
        return message;
    }

    public List<Message> findByMatchIdOrderByCreatedAtAsc(String matchId) {
        return messageTable.query(QueryConditional.sortBeginsWith(Key.builder()
                .partitionValue("MATCH#" + matchId)
                .sortValue("MSG#")
                .build()))
                .stream()
                .flatMap(p -> p.items().stream())
                .collect(Collectors.toList());
    }
}
