package com.example.demo.service;

import com.example.demo.Dto.ExchangeHistoryResponse;
import com.example.demo.models.Currency;
import com.example.demo.models.Exchange;
import com.example.demo.models.ExchangeRateHistory;
import com.example.demo.repository.CurrencyRepository;
import com.example.demo.repository.ExchangeRateHistoryRepository;
import com.example.demo.repository.ExchangeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ExchangeService {

    private final ExchangeRepository exchangeRepository;
    private final CurrencyRepository currencyRepository;
    private final ExchangeRateHistoryRepository exchangeRateHistoryRepository;
    private final ExchangeRateService exchangeRateService;

    public ExchangeService(ExchangeRepository exchangeRepository,
                           CurrencyRepository currencyRepository,
                           ExchangeRateHistoryRepository exchangeRateHistoryRepository,
                           ExchangeRateService exchangeRateService) {
        this.exchangeRepository = exchangeRepository;
        this.currencyRepository = currencyRepository;
        this.exchangeRateHistoryRepository = exchangeRateHistoryRepository;
        this.exchangeRateService = exchangeRateService;
    }

    public double getLatestRate(Currency from, Currency to) {
        if (from.getId().equals(to.getId())) {
            return 1.0;
        }

        return exchangeRepository.findByFromCurrencyAndToCurrency(from, to)
                .map(Exchange::getRate)
                .orElseThrow(() -> new RuntimeException(
                        "Exchange rate not found for " + from.getName() + " -> " + to.getName()
                ));
    }

    @Transactional
    public Exchange syncLatestRate(String from, String to) {
        Currency fromCurrency = currencyRepository.findByName(from)
                .orElseThrow(() -> new RuntimeException("From currency not found"));

        Currency toCurrency = currencyRepository.findByName(to)
                .orElseThrow(() -> new RuntimeException("To currency not found"));

        Double latestRate = exchangeRateService.getLatestRate(from, to);

        Exchange exchange = exchangeRepository.findByFromCurrencyAndToCurrency(fromCurrency, toCurrency)
                .orElseGet(Exchange::new);

        Double oldRate = exchange.getRate();

        exchange.setFromCurrency(fromCurrency);
        exchange.setToCurrency(toCurrency);
        exchange.setRate(latestRate);

        Exchange saved = exchangeRepository.save(exchange);

        boolean rateChanged = oldRate == null || Double.compare(oldRate, latestRate) != 0;

        if (rateChanged) {
            ExchangeRateHistory history = new ExchangeRateHistory();
            history.setFromCurrency(fromCurrency);
            history.setToCurrency(toCurrency);
            history.setRate(latestRate);
            history.setRateDate(LocalDate.now());
            history.setFetchedAt(LocalDateTime.now());
            history.setSourceName("FRANKFURTER");

            exchangeRateHistoryRepository.save(history);
        }

        return saved;
    }

    @Transactional
    public Exchange saveManualRate(String from, String to, Double rate) {
        Currency fromCurrency = currencyRepository.findByName(from)
                .orElseThrow(() -> new RuntimeException("From currency not found"));

        Currency toCurrency = currencyRepository.findByName(to)
                .orElseThrow(() -> new RuntimeException("To currency not found"));

        Exchange exchange = exchangeRepository.findByFromCurrencyAndToCurrency(fromCurrency, toCurrency)
                .orElseGet(Exchange::new);

        Double oldRate = exchange.getRate();

        exchange.setFromCurrency(fromCurrency);
        exchange.setToCurrency(toCurrency);
        exchange.setRate(rate);

        Exchange saved = exchangeRepository.save(exchange);

        boolean rateChanged = oldRate == null || Double.compare(oldRate, rate) != 0;

        if (rateChanged) {
            ExchangeRateHistory history = new ExchangeRateHistory();
            history.setFromCurrency(fromCurrency);
            history.setToCurrency(toCurrency);
            history.setRate(rate);
            history.setRateDate(LocalDate.now());
            history.setFetchedAt(LocalDateTime.now());
            history.setSourceName("MANUAL");

            exchangeRateHistoryRepository.save(history);
        }

        return saved;
    }

    public List<ExchangeHistoryResponse> getHistory(String from, String to) {
        Currency fromCurrency = currencyRepository.findByName(from)
                .orElseThrow(() -> new RuntimeException("From currency not found"));

        Currency toCurrency = currencyRepository.findByName(to)
                .orElseThrow(() -> new RuntimeException("To currency not found"));

        return exchangeRateHistoryRepository
                .findByFromCurrencyAndToCurrencyOrderByRateDateDesc(fromCurrency, toCurrency)
                .stream()
                .map(item -> new ExchangeHistoryResponse(
                        item.getId(),
                        item.getFromCurrency().getName(),
                        item.getToCurrency().getName(),
                        item.getRate(),
                        item.getRateDate(),
                        item.getFetchedAt(),
                        item.getSourceName()
                ))
                .toList();
    }
}