package com.example.demo.repository;

import com.example.demo.models.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WalletRepository extends JpaRepository<Wallet, UUID> {

    List<Wallet> findByUserId(UUID userId);

    // ✅ Correct method name
    Optional<Wallet> findByUserIdAndCurrencyIdAndWalletType(
            UUID userId,
            UUID currencyId,
            String walletType);
}