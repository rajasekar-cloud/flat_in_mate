package com.flatmate.app.swipe;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

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

    /** Returns the existing swipe for a seeker-listing pair, if any. */
    public Optional<Swipe> findBySeekIdAndListingId(String seekerId, String listingId) {
        Key key = Key.builder()
                .partitionValue("USER#" + seekerId)
                .sortValue("SWIPE#" + listingId)
                .build();
        return Optional.ofNullable(swipeTable.getItem(key));
    }

    /** Returns the set of listingIds this seeker has already swiped on (any direction). */
    public Set<String> findSwipedListingIds(String seekerId) {
        return swipeTable.query(QueryConditional.sortBeginsWith(Key.builder()
                .partitionValue("USER#" + seekerId)
                .sortValue("SWIPE#")
                .build()))
                .stream()
                .flatMap(p -> p.items().stream())
                .map(Swipe::getListingId)
                .collect(Collectors.toSet());
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

    public List<Swipe> findBySeekerId(String seekerId) {
        return swipeTable.query(QueryConditional.sortBeginsWith(Key.builder()
                .partitionValue("USER#" + seekerId)
                .sortValue("SWIPE#")
                .build()))
                .stream()
                .flatMap(p -> p.items().stream())
                .collect(Collectors.toList());
    }

    public List<Swipe> findByListingId(String listingId) {
        return swipeTable.index("ListingIndex")
                .query(QueryEnhancedRequest.builder()
                        .queryConditional(QueryConditional.keyEqualTo(Key.builder()
                                .partitionValue(listingId)
                                .build()))
                        .build())
                .stream()
                .flatMap(p -> p.items().stream())
                .collect(Collectors.toList());
    }
}
