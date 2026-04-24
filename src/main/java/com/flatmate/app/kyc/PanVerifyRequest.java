package com.flatmate.app.kyc;

import lombok.Data;

@Data
public class PanVerifyRequest {
    private String userId;
    private String panNumber;
}
