package com.flatmate.app.kyc;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DlVerifyRequest {
    private String userId;
    private String dlNumber;
    private String dob;

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getDlNumber() { return dlNumber; }
    public void setDlNumber(String dlNumber) { this.dlNumber = dlNumber; }
    public String getDob() { return dob; }
    public void setDob(String dob) { this.dob = dob; }
}
