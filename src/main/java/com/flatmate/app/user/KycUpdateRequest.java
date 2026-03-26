package com.flatmate.app.user;

import lombok.Data;

@Data
public class KycUpdateRequest {
    private String userId;
    private String documentType;
    private String documentImageUrl;
    private String selfieImageUrl;
}
