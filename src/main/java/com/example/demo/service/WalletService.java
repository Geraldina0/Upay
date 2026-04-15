package com.example.demo.service;

import com.example.demo.Dto.WalletRequest;
import com.example.demo.models.*;
import com.example.demo.repository.CurrencyRepository;
import com.example.demo.repository.TransactionRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.WalletRepository;
import com.example.demo.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.demo.models.TransactionType;
import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class WalletService {

    private static final Logger logger = LoggerFactory.getLogger(WalletService.class);

    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final CurrencyRepository currencyRepository;
    private final JwtUtil jwtUtil;

    public WalletService(CurrencyRepository currencyRepository,WalletRepository walletRepository, UserRepository userRepository, JwtUtil jwtUtil, TransactionRepository transactionRepository) {
        this.walletRepository = walletRepository;
        this.transactionRepository = transactionRepository;
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
        this.currencyRepository = currencyRepository;
    }



    public List<Wallet> getWallets(String token) {
        // 1. Nxjerr emailin nga JWT
        String email = jwtUtil.extractEmail(token);

        // 2. Merr User nga DB
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 3. Merr wallets e këtij user-i
        return walletRepository.findByUserId(user.getId());
    }



    /**
     * Create a new wallet based on WalletRequest.
     *
     * @param walletRequest The request containing wallet data
     * @return The saved wallet object
     */
    public Wallet createWallet(WalletRequest walletRequest) {
        // Get the email from the walletRequest (email should have been set in the controller)
        String email = walletRequest.getEmail();
        String name = walletRequest.getCurrency();

        // Fetch the user by email
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Currency currency = currencyRepository.findByName(name)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Wallet wallet = new Wallet();
        wallet.setUser(user);
        wallet.setBalance(walletRequest.getBalance());
        wallet.setCurrency(currency);
        wallet.setIsActive(walletRequest.isActive());
        wallet.setWalletType(walletRequest.getWalletType());

        return walletRepository.save(wallet);
    }




}



