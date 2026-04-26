package com.example.demo.models;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "transactions")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "wallet_in", nullable = true)
    private Wallet walletIn;

    @ManyToOne
    @JoinColumn(name = "wallet_out", nullable = true)
    private Wallet walletOut;

    @Column(nullable = false, unique = true)
    private UUID transactionId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType transactionType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionStatus status;

    @Column(name = "amount_transferred", nullable = false, precision = 19, scale = 4)
    private BigDecimal amountTransferred;

    @Column(precision = 19, scale = 8)
    private BigDecimal exchangeRate;

    @Column(name = "date", nullable = false)
    private LocalDate date;

    public UUID getId() {
        return id;
    }

    public UUID getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(UUID transactionId) {
        this.transactionId = transactionId;
    }

    public Wallet getWalletIn() {
        return walletIn;
    }

    public void setWalletIn(Wallet walletIn) {
        this.walletIn = walletIn;
    }

    public Wallet getWalletOut() {
        return walletOut;
    }

    public void setWalletOut(Wallet walletOut) {
        this.walletOut = walletOut;
    }

    public TransactionType getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(TransactionType transactionType) {
        this.transactionType = transactionType;
    }

    public TransactionStatus getStatus() {
        return status;
    }

    public void setStatus(TransactionStatus status) {
        this.status = status;
    }

    public BigDecimal getAmountTransferred() {
        return amountTransferred;
    }

    public void setAmountTransferred(BigDecimal amountTransferred) {
        this.amountTransferred = amountTransferred;
    }



    public BigDecimal getExchangeRate() {
        return exchangeRate;
    }

    public void setExchangeRate(BigDecimal exchangeRate) {
        this.exchangeRate = exchangeRate;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }
}