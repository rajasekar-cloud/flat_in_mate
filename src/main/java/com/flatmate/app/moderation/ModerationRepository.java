package com.flatmate.app.moderation;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.*;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;

import java.util.List;
import java.util.stream.Collectors;

@Repository
public class ModerationRepository {

    private final DynamoDbTable<Block> blockTable;
    private final DynamoDbTable<Report> reportTable;

    public ModerationRepository(DynamoDbEnhancedClient enhancedClient,
                                @Value("${aws.dynamodb.table-name}") String tableName) {
        this.blockTable = enhancedClient.table(tableName, TableSchema.fromBean(Block.class));
        this.reportTable = enhancedClient.table(tableName, TableSchema.fromBean(Report.class));
    }

    public Block blockUser(Block block) {
        blockTable.putItem(block);
        return block;
    }

    public void unblockUser(String blockerId, String blockedId) {
        blockTable.deleteItem(Key.builder()
                .partitionValue("BLOCK#" + blockerId)
                .sortValue("BLOCKED#" + blockedId)
                .build());
    }

    public List<String> getBlockedUserIds(String blockerId) {
        return blockTable.query(QueryConditional.keyEqualTo(Key.builder()
                        .partitionValue("BLOCK#" + blockerId)
                        .build()))
                .items().stream()
                .map(Block::getBlockedId)
                .collect(Collectors.toList());
    }

    public Report saveReport(Report report) {
        reportTable.putItem(report);
        return report;
    }
}
