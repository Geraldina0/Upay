package com.example.demo.controller;

import com.example.demo.Dto.TransactionRequest;
import com.example.demo.Dto.WalletRequest;
import com.example.demo.models.Wallet;
import com.example.demo.service.WalletService;
import com.example.demo.util.JwtUtil;
import com.example.demo.websocket.CustomWebSocketHandler;
import jakarta.servlet.http.HttpServletRequest;
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
    private final JwtUtil jwtUtil;
    private static final Logger logger = LoggerFactory.getLogger(WalletController.class);

    private final CustomWebSocketHandler customWebSocketHandler;

    public WalletController(WalletService walletService, JwtUtil jwtUtil,CustomWebSocketHandler customWebSocketHandler) {
        this.walletService = walletService;
        this.jwtUtil = jwtUtil;
        this.customWebSocketHandler = customWebSocketHandler;

    }
    @GetMapping
    public ResponseEntity<List<Wallet>> getWallets(
            @RequestHeader("Authorization") String authorizationHeader) {

        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String token = authorizationHeader.substring(7); // heq "Bearer "
        List<Wallet> wallets = walletService.getWallets(token);

        return ResponseEntity.ok(wallets);
    }




    @PostMapping("/create")
    public ResponseEntity<Wallet> createWallet(@RequestBody WalletRequest walletRequest, HttpServletRequest request) {
        // Extract the token from the request header
        String token = request.getHeader("Authorization").substring(7); // Remove "Bearer " prefix
        System.out.println("Received Token: " + token);  // Log the token

        // Get the email from the token (assuming subject is email)
        String email = jwtUtil.extractEmail(token);  // Directly use the email from JWT
        System.out.println("Extracted Email: " + email);  // Log the email extracted from token

        // Set email in walletRequest
        walletRequest.setEmail(email);

        // Call the service to create the wallet, passing the walletRequest object
        Wallet wallet = walletService.createWallet(walletRequest);
        return ResponseEntity.ok(wallet);
    }



}