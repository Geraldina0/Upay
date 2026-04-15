package com.example.demo.models;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "exchange")
public class Exchange {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "from_currency", nullable = false)
    private Currency fromCurrency;

    @ManyToOne
    @JoinColumn(name = "to_currency", nullable = false)
    private Currency toCurrency;

    @Column(nullable = false)
    private Double rate;

    public UUID getId() {
        return id;
    }

    public Currency getFromCurrency() {
        return fromCurrency;
    }

    public Currency getToCurrency() {
        return toCurrency;
    }

    public Double getRate() {
        return rate;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public void setFromCurrency(Currency fromCurrency) {
        this.fromCurrency = fromCurrency;
    }

    public void setToCurrency(Currency toCurrency) {
        this.toCurrency = toCurrency;
    }

    public void setRate(Double rate) {
        this.rate = rate;
    }
}