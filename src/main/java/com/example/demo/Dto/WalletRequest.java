package com.example.demo.Dto;

import java.math.BigDecimal;

public class WalletRequest {
    private BigDecimal balance;
    private String currency;
    private boolean isActive;
    private String walletType;


    public WalletRequest(BigDecimal balance, String currency, boolean isActive, String walletType) {
        this.balance = balance;
        this.currency = currency;
        this.isActive = isActive;
        this.walletType = walletType;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public String getWalletType() {
        return walletType;
    }

    public void setWalletType(String walletType) {
        this.walletType = walletType;
    }
}