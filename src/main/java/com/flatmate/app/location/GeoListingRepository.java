package com.flatmate.app.location;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;

import java.util.List;
import java.util.stream.Collectors;

@Repository
public class GeoListingRepository {

    private final DynamoDbTable<GeoListing> geoTable;

    public GeoListingRepository(DynamoDbEnhancedClient enhancedClient,
            @Value("${aws.dynamodb.table-name}") String tableName) {
        this.geoTable = enhancedClient.table(tableName, TableSchema.fromBean(GeoListing.class));
    }

    public void save(GeoListing geo) {
        geoTable.putItem(geo);
    }

    public List<GeoListing> findAll() {
        return geoTable.scan().items().stream()
                .filter(g -> g.getPk().startsWith("GEOLOC#"))
                .collect(Collectors.toList());
    }
}
