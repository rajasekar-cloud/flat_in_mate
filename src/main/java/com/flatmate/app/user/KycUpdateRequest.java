package com.flatmate.app.user;

import lombok.Data;

public class KycUpdateRequest {
    private String userId;
    private String documentType;
    private String documentImageUrl;
    private String selfieImageUrl;

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getDocumentType() { return documentType; }
    public void setDocumentType(String documentType) { this.documentType = documentType; }
    public String getDocumentImageUrl() { return documentImageUrl; }
    public void setDocumentImageUrl(String documentImageUrl) { this.documentImageUrl = documentImageUrl; }
    public String getSelfieImageUrl() { return selfieImageUrl; }
    public void setSelfieImageUrl(String selfieImageUrl) { this.selfieImageUrl = selfieImageUrl; }
}
