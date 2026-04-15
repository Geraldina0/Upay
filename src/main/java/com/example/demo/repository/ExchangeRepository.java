package com.example.demo.repository;

import com.example.demo.models.Currency;
import com.example.demo.models.Exchange;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ExchangeRepository extends JpaRepository<Exchange, UUID> {
    Optional<Exchange> findByFromCurrencyAndToCurrency(Currency from, Currency to);
}
