package com.example.demo.controller;

import com.example.demo.Dto.WalletRequest;
import com.example.demo.models.Wallet;
import com.example.demo.service.WalletService;
import com.example.demo.websocket.CustomWebSocketHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("wallets")
public class WalletController {

    private final WalletService walletService;
    private static final Logger logger = LoggerFactory.getLogger(WalletController.class);
    private final CustomWebSocketHandler customWebSocketHandler;

    public WalletController(WalletService walletService,
                            CustomWebSocketHandler customWebSocketHandler) {
        this.walletService = walletService;
        this.customWebSocketHandler = customWebSocketHandler;
    }

    @GetMapping
    public ResponseEntity<List<Wallet>> getWallets(
            @RequestHeader("Authorization") String authorizationHeader) {

        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String token = authorizationHeader.substring(7).trim();
        List<Wallet> wallets = walletService.getWallets(token);

        return ResponseEntity.ok(wallets);
    }

    @PostMapping("/create")
    public ResponseEntity<?> createWallet(@RequestBody WalletRequest walletRequest,
                                          @RequestHeader("Authorization") String authorizationHeader) {
        try {
            if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid or missing token");
            }

            String token = authorizationHeader.substring(7).trim();

            Wallet wallet = walletService.createWallet(token, walletRequest);
            return ResponseEntity.status(HttpStatus.CREATED).body(wallet);

        } catch (RuntimeException e) {
            logger.error("Wallet creation failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());

        } catch (Exception e) {
            logger.error("Unexpected error during wallet creation", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An unexpected error occurred");
        }
    }
}