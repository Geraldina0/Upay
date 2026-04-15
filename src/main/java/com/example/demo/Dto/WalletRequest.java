package com.example.demo.Dto;

public class WalletRequest {
    private double balance;
    private String currency;
    private boolean isActive;
    private String walletType;
    private String email; // We can use the email from the JWT instead of the userId.
    private double previous_balance;
    private double transaction_amount;

    // Constructors
    public WalletRequest(double balance, String currency, boolean isActive, String walletType, double previous_balance, double transaction_amount) {
        this.balance = balance;
        this.currency = currency;
        this.isActive = isActive;
        this.walletType = walletType;
        this.previous_balance = previous_balance;
        this.transaction_amount = transaction_amount;
    }

    // Getters and Setters
    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }


}
