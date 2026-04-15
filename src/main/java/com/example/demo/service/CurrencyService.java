package com.example.demo.service;

import com.example.demo.models.Currency;
import com.example.demo.repository.CurrencyRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class CurrencyService {

    private final CurrencyRepository currencyRepository;

    public CurrencyService(CurrencyRepository currencyRepository) {
        this.currencyRepository = currencyRepository;
    }

    public List<Currency> getAllCurrencies() {
        return currencyRepository.findAll();
    }

    public Currency createCurrency(String name) {
        // Check if currency with the same name already exists
        Optional<Currency> existingCurrency = currencyRepository.findByName(name);

        if (existingCurrency.isPresent()) {
            throw new RuntimeException("Currency with name '" + name + "' already exists.");
            // Alternatively, you can return a specific error response instead of throwing an exception
        }

        // Create and save the new currency if it doesn't already exist
        Currency newCurrency = new Currency(); // Use no-argument constructor
        newCurrency.setName(name); // Set name separately
        newCurrency.setStatus(Currency.Status.ACTIVE); // Set status separately
        return currencyRepository.save(newCurrency);
    }

    // Activate or deactivate a currency
    public Optional<Currency> updateCurrencyStatus(UUID id, Currency.Status status) {
        return currencyRepository.findById(id).map(currency -> {
            currency.setStatus(status);
            return currencyRepository.save(currency);
        });
    }
}
