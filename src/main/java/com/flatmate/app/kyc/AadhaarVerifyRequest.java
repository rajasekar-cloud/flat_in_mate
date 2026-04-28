package com.flatmate.app.kyc;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AadhaarVerifyRequest {
    private String userId;
    private String clientId;
    private String otp;

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getClientId() { return clientId; }
    public void setClientId(String clientId) { this.clientId = clientId; }
    public String getOtp() { return otp; }
    public void setOtp(String otp) { this.otp = otp; }
}
