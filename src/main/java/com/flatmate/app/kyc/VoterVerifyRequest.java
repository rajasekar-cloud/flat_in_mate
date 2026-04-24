package com.flatmate.app.kyc;

import lombok.Data;

@Data
public class VoterVerifyRequest {
    private String userId;
    private String voterId;
}
