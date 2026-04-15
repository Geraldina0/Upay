package com.example.demo.Dto;

import java.util.UUID;

public class OtpResponse {

    private String  email;
    private String otp;
    private UUID transactionId;


    public OtpResponse(String email, String otp, UUID transactionId) {
        this.email = email;
        this.otp = otp;
        this.transactionId=transactionId;
    }



    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public UUID getTransactionId(){return transactionId;}
    public void setTransactionId(UUID transactionId){this.transactionId=transactionId;}
    public String getOtp() {
        return otp;
    }
    public void setOtp(String otp) {
        this.otp = otp;
    }


}
