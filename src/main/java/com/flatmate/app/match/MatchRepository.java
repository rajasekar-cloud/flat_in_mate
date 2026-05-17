package com.flatmate.app.match;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;

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
        return findByUserId(seekerId).stream()
                .filter(m -> m.getMatchId().equals(id))
                .findFirst();
    }

    public List<Match> findBySeekerId(String seekerId) {
        return findAll().stream()
                .filter(m -> seekerId.equals(m.getSeekerId()))
                .collect(Collectors.toList());
    }

    public List<Match> findByUserId(String userId) {
        return findAll().stream()
                .filter(m -> userId.equals(m.getSeekerId()) || userId.equals(m.getOwnerId()))
                .collect(Collectors.toList());
    }

    public Optional<Match> findBySeekerIdAndOwnerId(String seekerId, String ownerId) {
        return findAll().stream()
                .filter(m -> seekerId.equals(m.getSeekerId()))
                .filter(m -> ownerId.equals(m.getOwnerId()))
                .findFirst();
    }

    private List<Match> findAll() {
        return matchTable.scan().items().stream()
                .filter(m -> m.getMatchId() != null)
                .collect(Collectors.toList());
    }
}
