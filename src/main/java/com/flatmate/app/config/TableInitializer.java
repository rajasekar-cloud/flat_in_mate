package com.flatmate.app.config;

import com.flatmate.app.auth.User;
import com.flatmate.app.chat.Message;
import com.flatmate.app.listing.Listing;
import com.flatmate.app.location.GeoListing;
import com.flatmate.app.match.Match;
import com.flatmate.app.notification.DeviceToken;
import com.flatmate.app.payment.Subscription;
import com.flatmate.app.user.GlobalCounter;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.services.dynamodb.model.ResourceInUseException;

@Configuration
@RequiredArgsConstructor
public class TableInitializer {

    private final DynamoDbEnhancedClient enhancedClient;

    @Value("${aws.dynamodb.table-name}")
    private String tableName;

    @PostConstruct
    public void init() {
        createTable(User.class);
    }

    private <T> void createTable(Class<T> type) {
        DynamoDbTable<T> table = enhancedClient.table(tableName, TableSchema.fromBean(type));
        try {
            table.createTable();
            System.out.println("Created DynamoDB table: " + tableName);
        } catch (ResourceInUseException e) {
            System.out.println("DynamoDB table already exists: " + tableName);
        }
    }
}
