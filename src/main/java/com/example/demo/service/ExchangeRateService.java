package com.example.demo.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.Map;

@Service
public class ExchangeRateService {

    private static final String BASE_URL = "https://api.frankfurter.dev/v1";
    private final RestTemplate restTemplate = new RestTemplate();

    public Double getLatestRate(String fromCode, String toCode) {
        String url = BASE_URL + "/latest?base=" + fromCode + "&symbols=" + toCode;

        Map<String, Object> response = restTemplate.getForObject(url, Map.class);
        if (response == null || !response.containsKey("rates")) {
            throw new RuntimeException("Failed to fetch latest exchange rate");
        }

        Map<String, Object> rates = (Map<String, Object>) response.get("rates");
        Object rate = rates.get(toCode);

        if (rate == null) {
            throw new RuntimeException("Rate not found for " + fromCode + " -> " + toCode);
        }

        return ((Number) rate).doubleValue();
    }

    public Double getHistoricalRate(String fromCode, String toCode, LocalDate date) {
        String url = BASE_URL + "/" + date + "?base=" + fromCode + "&symbols=" + toCode;

        Map<String, Object> response = restTemplate.getForObject(url, Map.class);
        if (response == null || !response.containsKey("rates")) {
            throw new RuntimeException("Failed to fetch historical exchange rate");
        }

        Map<String, Object> rates = (Map<String, Object>) response.get("rates");
        Object rate = rates.get(toCode);

        if (rate == null) {
            throw new RuntimeException("Historical rate not found for " + fromCode + " -> " + toCode);
        }

        return ((Number) rate).doubleValue();
    }
}