package com.flatmate.app.listing;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.PageIterable;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class ListingRepository {

    private final DynamoDbTable<Listing> listingTable;

    public ListingRepository(DynamoDbEnhancedClient enhancedClient,
            @Value("${aws.dynamodb.table-name}") String tableName) {
        this.listingTable = enhancedClient.table(tableName, TableSchema.fromBean(Listing.class));
    }

    public Listing save(Listing listing) {
        listingTable.putItem(listing);
        return listing;
    }

    public Optional<Listing> findById(String id) {
        return Optional.ofNullable(listingTable.getItem(Key.builder()
                .partitionValue("LISTING#" + id)
                .sortValue("METADATA")
                .build()));
    }

    public List<Listing> findAll() {
        // In a real Single Table design, we might use a GSI to scan all listings.
        // For now, we'll scan the table and filter for LISTING# prefix.
        return listingTable.scan().items().stream()
                .filter(l -> l.getPk().startsWith("LISTING#"))
                .collect(Collectors.toList());
    }
}
