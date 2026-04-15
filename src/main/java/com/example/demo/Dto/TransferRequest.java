package com.example.demo.Dto;

import java.util.UUID;

public class TransferRequest {
    private UUID senderWalletId;
    private UUID receiverWalletId;
    private Double amountTransferred;

    public UUID getSenderWalletId() {
        return senderWalletId;
    }

    public UUID getReceiverWalletId() {
        return receiverWalletId;
    }

    public Double getAmountTransferred() {
        return amountTransferred;
    }
}
