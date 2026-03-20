package com.flatmate.app.match;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class MatchRepository {

    private final DynamoDbTable<Match> matchTable;

    public MatchRepository(DynamoDbEnhancedClient enhancedClient,
            @Value("${aws.dynamodb.table-name}") String tableName) {
        this.matchTable = enhancedClient.table(tableName, TableSchema.fromBean(Match.class));
    }

    public Match save(Match match) {
        matchTable.putItem(match);
        return match;
    }

    public Optional<Match> findById(String id, String seekerId) {
        // In this design, PK=USER#seekerId, SK=MATCH#ownerId.
        // We'd need a secondary index to find by matchId alone, or store matchId in SK.
        // For simplicity, we query by seekerId.
        return findBySeekerId(seekerId).stream()
                .filter(m -> m.getMatchId().equals(id))
                .findFirst();
    }

    public List<Match> findBySeekerId(String seekerId) {
        return matchTable.query(QueryConditional.sortBeginsWith(Key.builder()
                .partitionValue("USER#" + seekerId)
                .sortValue("MATCH#")
                .build()))
                .stream()
                .flatMap(p -> p.items().stream())
                .collect(Collectors.toList());
    }

    public Optional<Match> findBySeekerIdAndOwnerId(String seekerId, String ownerId) {
        Match match = matchTable.getItem(Key.builder()
                .partitionValue("USER#" + seekerId)
                .sortValue("MATCH#" + ownerId)
                .build());
        return Optional.ofNullable(match);
    }
}
