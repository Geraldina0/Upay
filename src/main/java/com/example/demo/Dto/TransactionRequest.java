package com.example.demo.Dto;

import java.math.BigDecimal;
import java.util.UUID;

public class TransactionRequest {

    private UUID senderWalletId;
    private UUID receiverWalletId;
    private UUID userWalletId;
    private BigDecimal amount;
    private String transactionType;
    private String status;

    public UUID getSenderWalletId() {
        return senderWalletId;
    }

    public void setSenderWalletId(UUID senderWalletId) {
        this.senderWalletId = senderWalletId;
    }

    public UUID getReceiverWalletId() {
        return receiverWalletId;
    }

    public void setReceiverWalletId(UUID receiverWalletId) {
        this.receiverWalletId = receiverWalletId;
    }

    public UUID getUserWalletId() {
        return userWalletId;
    }

    public void setUserWalletId(UUID userWalletId) {
        this.userWalletId = userWalletId;
    }

    public BigDecimal getAmount() {
        return amount;
    }



    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }


    public String getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}