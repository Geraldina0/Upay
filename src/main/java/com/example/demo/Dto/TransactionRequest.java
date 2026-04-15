package com.example.demo.Dto;

import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDate;
import java.util.UUID;

public class TransactionRequest {
    private UUID adminWalletId;
    private UUID userWalletId;
    private double amount;
    private String transactionType;
    private String status;


    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate date;

    // Getters and Setters

    public UUID getAdminWalletId() {
        return adminWalletId;
    }

    public void setAdminWalletId(UUID adminWalletId) {
        this.adminWalletId = adminWalletId;
    }

    public UUID getUserWalletId() {
        return userWalletId;
    }

    public void setUserWalletId(UUID userWalletId) {
        this.userWalletId = userWalletId;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
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

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

}
