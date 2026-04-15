package com.example.demo.Dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public class ExchangeHistoryResponse {

    private UUID id;
    private String fromCurrency;
    private String toCurrency;
    private Double rate;
    private LocalDate rateDate;
    private LocalDateTime fetchedAt;
    private String sourceName;

    public ExchangeHistoryResponse(UUID id, String fromCurrency, String toCurrency,
                                   Double rate, LocalDate rateDate,
                                   LocalDateTime fetchedAt, String sourceName) {
        this.id = id;
        this.fromCurrency = fromCurrency;
        this.toCurrency = toCurrency;
        this.rate = rate;
        this.rateDate = rateDate;
        this.fetchedAt = fetchedAt;
        this.sourceName = sourceName;
    }

    public UUID getId() {
        return id;
    }

    public String getFromCurrency() {
        return fromCurrency;
    }

    public String getToCurrency() {
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
}