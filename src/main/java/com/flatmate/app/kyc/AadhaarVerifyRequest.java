package com.flatmate.app.kyc;

import lombok.Data;

@Data
public class AadhaarVerifyRequest {
    private String userId;
    private String clientId;   // returned by generate-otp step from SurePass
    private String otp;
}
