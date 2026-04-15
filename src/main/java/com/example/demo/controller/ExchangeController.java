package com.example.demo.controller;

import com.example.demo.service.ExchangeService;
import com.example.demo.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/exchange")
public class ExchangeController {

    private static final Logger logger = LoggerFactory.getLogger(ExchangeController.class);

    private final ExchangeService exchangeService;
    private final UserService userService;

    public ExchangeController(ExchangeService exchangeService, UserService userService) {
        this.exchangeService = exchangeService;
        this.userService = userService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> addExchangeRate(@RequestHeader("Authorization") String token,
                                             @RequestParam String from,
                                             @RequestParam String to,
                                             @RequestParam Double rate) {
        try {
            if (!userService.isAdmin(token)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Access denied. Admins only."));
            }

            var saved = exchangeService.saveManualRate(from, to, rate);

            return ResponseEntity.status(HttpStatus.CREATED).body(
                    Map.of(
                            "message", String.format("Manual exchange rate saved: 1 %s = %.4f %s",
                                    saved.getFromCurrency().getName(),
                                    saved.getRate(),
                                    saved.getToCurrency().getName()
                            )
                    )
            );
        } catch (RuntimeException e) {
            logger.error("Failed to save manual exchange rate: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error saving exchange rate", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error saving exchange rate"));
        }
    }

    @PostMapping("/sync")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> syncExchangeRate(@RequestHeader("Authorization") String token,
                                              @RequestParam String from,
                                              @RequestParam String to) {
        try {
            if (!userService.isAdmin(token)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Access denied. Admins only."));
            }

            var saved = exchangeService.syncLatestRate(from, to);

            return ResponseEntity.ok(
                    Map.of(
                            "message", String.format("Live exchange rate synced: 1 %s = %.4f %s",
                                    saved.getFromCurrency().getName(),
                                    saved.getRate(),
                                    saved.getToCurrency().getName()
                            )
                    )
            );
        } catch (RuntimeException e) {
            logger.error("Failed to sync exchange rate: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error syncing exchange rate", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error syncing exchange rate"));
        }
    }

    @GetMapping("/history")
    public ResponseEntity<?> getExchangeHistory(@RequestParam String from,
                                                @RequestParam String to) {
        try {
            return ResponseEntity.ok(exchangeService.getHistory(from, to));
        } catch (RuntimeException e) {
            logger.error("Failed to get exchange history: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error loading exchange history", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error loading exchange history"));
        }
    }
}