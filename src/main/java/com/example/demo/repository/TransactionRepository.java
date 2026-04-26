package com.example.demo.repository;

import com.example.demo.models.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface TransactionRepository extends JpaRepository<Transaction, UUID> {

    List<Transaction> findByWalletOut_User_Id(UUID userId);

    List<Transaction> findByWalletIn_User_Id(UUID userId);

    @Query("""
        SELECT t
        FROM Transaction t
        WHERE
            (t.walletOut IS NOT NULL AND t.walletOut.user.id = :userId)
            OR
            (t.walletIn IS NOT NULL AND t.walletIn.user.id = :userId)
    """)
    List<Transaction> findAllByUserId(@Param("userId") UUID userId);

    @Query(value = "EXEC GetFilteredTransactions :transaction_type, :status, :user_id", nativeQuery = true)
    List<Transaction> getFilteredTransactions(
            @Param("transaction_type") String transactionType,
            @Param("status") String status,
            @Param("user_id") UUID userId
    );
}