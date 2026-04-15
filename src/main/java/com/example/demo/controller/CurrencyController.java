package com.example.demo.controller;

import com.example.demo.models.Currency;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.CurrencyService;
import com.example.demo.service.UserService;
import com.example.demo.util.JwtUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/currency")
public class CurrencyController {

    private final CurrencyService currencyService;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final UserService userService;

    public CurrencyController(CurrencyService currencyService,UserService userService, JwtUtil jwtUtil, UserRepository userRepository) {
        this.currencyService = currencyService;
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
        this.userService= userService;
    }

    // ✅ Get all currencies (USER and ADMIN)
    @GetMapping
    public ResponseEntity<?> getAllCurrencies(@RequestHeader("Authorization") String authorizationHeader) {
        try {
            if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Missing or invalid token");
            }

            String token = authorizationHeader.substring(7);
            String email = jwtUtil.extractEmail(token);

            userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found with email: " + email));

            return ResponseEntity.ok(currencyService.getAllCurrencies());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error fetching currencies");
        }
    }

    // ✅ Create new currency (ADMIN only, requires token + name parameter)
    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN')") // or hasAuthority("ADMIN") depending on your DB roles
    public ResponseEntity<?> createCurrency(@RequestHeader("Authorization") String token,
                                            @RequestParam String name) {
        try {
            if (!userService.isAdmin(token)) {
                return ResponseEntity.status(403).body(Map.of("error", "Access denied. Admins only."));
            }

            // Create and save new currency
            Currency savedCurrency = currencyService.createCurrency(name);

            return ResponseEntity.status(HttpStatus.CREATED).body(savedCurrency);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error creating currency");
        }
    }

    // ✅ Activate or deactivate a currency (ADMIN only)
    // ✅ Activate or deactivate a currency (ADMIN only)
    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')") // or hasAuthority("ADMIN")
    public ResponseEntity<?> updateCurrencyStatus(@RequestHeader("Authorization") String authorizationHeader,
                                                  @PathVariable UUID id,
                                                  @RequestParam String status) {
        try {
            if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Missing or invalid token");
            }

            // Convert status string to enum safely
            Currency.Status currencyStatus;
            try {
                currencyStatus = Currency.Status.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().body("Invalid status. Use 'ACTIVE' or 'INACTIVE'.");
            }

            // Call service with correct enum type
            Optional<Currency> updatedCurrency = currencyService.updateCurrencyStatus(id, currencyStatus);

            if (updatedCurrency.isPresent()) {
                return ResponseEntity.ok(updatedCurrency.get()); // ✅ returns Currency
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Currency not found with id: " + id); // ✅ returns String
            }

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error updating currency status");
        }
    }

}
