package com.flatmate.app.kyc;

import lombok.Data;

@Data
public class DlVerifyRequest {
    private String userId;
    private String dlNumber;
    private String dob;  // format: YYYY-MM-DD
}
