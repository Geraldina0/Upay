package com.example.demo.service;

import com.example.demo.Dto.WalletRequest;
import com.example.demo.models.Currency;
import com.example.demo.models.User;
import com.example.demo.models.Wallet;
import com.example.demo.repository.CurrencyRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.WalletRepository;
import com.example.demo.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class WalletService {

    private static final Logger logger = LoggerFactory.getLogger(WalletService.class);

    private final WalletRepository walletRepository;
    private final UserRepository userRepository;
    private final CurrencyRepository currencyRepository;
    private final JwtUtil jwtUtil;

    public WalletService(CurrencyRepository currencyRepository,
                         WalletRepository walletRepository,
                         UserRepository userRepository,
                         JwtUtil jwtUtil) {
        this.walletRepository = walletRepository;
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
        this.currencyRepository = currencyRepository;
    }

    public List<Wallet> getWallets(String token) {
        String email = jwtUtil.extractEmail(token);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return walletRepository.findByUserId(user.getId());
    }

    public Wallet createWallet(String token, WalletRequest walletRequest) {
        String email = jwtUtil.extractEmail(token);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (walletRequest == null) {
            throw new RuntimeException("Request body is missing");
        }

        if (walletRequest.getCurrency() == null || walletRequest.getCurrency().trim().isEmpty()) {
            throw new RuntimeException("Currency is required");
        }

        if (walletRequest.getWalletType() == null || walletRequest.getWalletType().trim().isEmpty()) {
            throw new RuntimeException("Wallet type is required");
        }

        String currencyName = walletRequest.getCurrency().trim();
        String walletType = walletRequest.getWalletType().trim();

        Currency currency = currencyRepository.findByName(currencyName)
                .orElseThrow(() -> new RuntimeException("Currency not found: " + currencyName));

        if (currency.getId() == null) {
            throw new RuntimeException("Currency found but ID is null");
        }

        walletRepository.findByUserIdAndCurrencyIdAndWalletType(user.getId(), currency.getId(), walletType)
                .ifPresent(existingWallet -> {
                    throw new RuntimeException("Wallet with this currency and type already exists");
                });

        Wallet wallet = new Wallet();
        wallet.setUser(user);
        wallet.setCurrency(currency);
        wallet.setWalletType(walletType);
        wallet.setBalance(BigDecimal.ZERO);
        wallet.setIsActive(true);

        logger.info("Creating wallet for user {} with currency {} and type {}",
                email, currency.getName(), walletType);

        return walletRepository.save(wallet);
    }
}