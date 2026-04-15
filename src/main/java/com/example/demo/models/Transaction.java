package com.example.demo.models;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "transactions")
public class Transaction {

    @Id
    @GeneratedValue
    private UUID id; // Unique transaction ID

    @ManyToOne
    @JoinColumn(name = "wallet_in", referencedColumnName = "id", nullable = false) // Ensuring wallet_in cannot be NULL
    private Wallet walletIn; // Associated Wallet receiving the money

    @ManyToOne
    @JoinColumn(name = "wallet_out", referencedColumnName = "id", nullable = false) // Ensuring wallet_out cannot be NULL
    private Wallet walletOut; // Associated Wallet sending the money

    @Column(nullable = true, unique = true)
    private UUID transactionId; // Another unique transaction identifier

    @Enumerated(EnumType.STRING)
    @Column(nullable = true)
    private TransactionType transactionType; // Type of transaction (kept only this field)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionStatus status; // Transaction status (PENDING, COMPLETED, etc.)

    @Column(nullable = false)
    private Double amount_transferred;

    @Column(name = "date", nullable = false)
    private LocalDate date;

    // Getters and Setters

    public void setTransactionId(UUID transactionId) {
        this.transactionId = transactionId;
    }

    public void setTransactionType(TransactionType transactionType) {
        this.transactionType = transactionType;
    }

    public void setAmount_transferred(Double amount_transferred) {
        this.amount_transferred = amount_transferred;
    }

    public UUID getTransactionId() {
        return this.transactionId;
    }

    public Double getAmount_transferred() {
        return this.amount_transferred;
    }

    public TransactionType getTransactionType() {
        return this.transactionType;
    }

    public TransactionStatus getStatus() {
        return status;
    }

    public void setStatus(TransactionStatus status) {
        this.status = status;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public Wallet getWalletIn() {
        return walletIn;
    }

    public Wallet getWalletOut() {
        return walletOut;
    }

    public void setWalletIn(Wallet walletIn) {
        this.walletIn = walletIn;
    }

    public void setWalletOut(Wallet walletOut) {
        this.walletOut = walletOut;
    }

    public void setType(TransactionType transactionType) {
        this.transactionType = transactionType;
    }


}
