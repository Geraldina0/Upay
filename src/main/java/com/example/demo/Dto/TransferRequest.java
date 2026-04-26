package com.example.demo.Dto;

import java.math.BigDecimal;
import java.util.UUID;

public class TransferRequest {
    private UUID senderWalletId;
    private UUID receiverWalletId;
    private BigDecimal amountTransferred;

    public UUID getSenderWalletId() {
        return senderWalletId;
    }

    public UUID getReceiverWalletId() {
        return receiverWalletId;
    }

    public BigDecimal getAmountTransferred() {
        return amountTransferred;
    }
}
