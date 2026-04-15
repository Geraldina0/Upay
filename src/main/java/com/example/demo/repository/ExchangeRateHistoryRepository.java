package com.example.demo.repository;

import com.example.demo.models.Currency;
import com.example.demo.models.ExchangeRateHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ExchangeRateHistoryRepository extends JpaRepository<ExchangeRateHistory, UUID> {

    List<ExchangeRateHistory> findByFromCurrencyAndToCurrencyOrderByRateDateDesc(
            Currency fromCurrency,
            Currency toCurrency
    );

    Optional<ExchangeRateHistory> findTopByFromCurrencyAndToCurrencyOrderByFetchedAtDesc(
            Currency fromCurrency,
            Currency toCurrency
    );
}