package com.example.demo.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.example.demo.models.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WalletRepository extends JpaRepository<Wallet, UUID> {
    List<Wallet> findByUserId(UUID userId);

}

