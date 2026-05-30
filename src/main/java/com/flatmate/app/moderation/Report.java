package com.flatmate.app.moderation;

import lombok.*;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.*;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@DynamoDbBean
public class Report {
    private String reportId;
    private String reporterId;
    private String reportedUserId;
    private String reportedListingId;
    private String reason;
    private String details;
    private String status; // PENDING, RESOLVED, DISMISSED
    private String createdAt;

    public String getReportId() { return reportId; }
    public void setReportId(String reportId) { this.reportId = reportId; }

    public String getReporterId() { return reporterId; }
    public void setReporterId(String reporterId) { this.reporterId = reporterId; }

    public String getReportedUserId() { return reportedUserId; }
    public void setReportedUserId(String reportedUserId) { this.reportedUserId = reportedUserId; }

    public String getReportedListingId() { return reportedListingId; }
    public void setReportedListingId(String reportedListingId) { this.reportedListingId = reportedListingId; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    @DynamoDbPartitionKey
    @DynamoDbAttribute("PK")
    public String getPk() {
        return "REPORT#" + reportId;
    }

    public void setPk(String pk) {}

    @DynamoDbSortKey
    @DynamoDbAttribute("SK")
    public String getSk() {
        return "METADATA";
    }

    public void setSk(String sk) {}
}
