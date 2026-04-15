package com.example.demo.models;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "exchange_rate_history")
public class ExchangeRateHistory {

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

    @Column(name = "rate_date", nullable = false)
    private LocalDate rateDate;

    @Column(name = "fetched_at", nullable = false)
    private LocalDateTime fetchedAt;

    @Column(name = "source_name", nullable = false)
    private String sourceName;

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

    public LocalDate getRateDate() {
        return rateDate;
    }

    public LocalDateTime getFetchedAt() {
        return fetchedAt;
    }

    public String getSourceName() {
        return sourceName;
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

    public void setRateDate(LocalDate rateDate) {
        this.rateDate = rateDate;
    }

    public void setFetchedAt(LocalDateTime fetchedAt) {
        this.fetchedAt = fetchedAt;
    }

    public void setSourceName(String sourceName) {
        this.sourceName = sourceName;
    }
}