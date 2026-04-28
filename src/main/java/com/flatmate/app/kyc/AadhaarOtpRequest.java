package com.flatmate.app.kyc;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AadhaarOtpRequest {
    private String aadhaarNumber;

    public String getAadhaarNumber() { return aadhaarNumber; }
    public void setAadhaarNumber(String aadhaarNumber) { this.aadhaarNumber = aadhaarNumber; }
}
