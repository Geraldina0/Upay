package com.example.demo.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class ExchangeRateScheduler {

    private static final Logger logger = LoggerFactory.getLogger(ExchangeRateScheduler.class);

    private final ExchangeService exchangeService;

    public ExchangeRateScheduler(ExchangeService exchangeService) {
        this.exchangeService = exchangeService;
    }

    @Scheduled(cron = "0 */1 * * * *") // çdo 1 minutë për testim
    public void syncPopularRates() {
        try {
            logger.info("Starting scheduled exchange rate sync...");

            exchangeService.syncLatestRate("EUR", "USD");
            exchangeService.syncLatestRate("USD", "EUR");
            exchangeService.syncLatestRate("EUR", "GBP");

            logger.info("Scheduled exchange rate sync completed successfully.");
        } catch (Exception e) {
            logger.error("Error during scheduled exchange rate sync: {}", e.getMessage(), e);
        }
    }
}