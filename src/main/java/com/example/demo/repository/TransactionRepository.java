package com.example.demo.repository;
import com.example.demo.models.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;


public interface TransactionRepository extends JpaRepository<Transaction, UUID> {

    // Custom query to find transactions by user ID, either as wallet_in or wallet_out
    List<Transaction> findByWalletOut_UserId(UUID walletOutUserId);

    @Query(value = "EXEC GetFilteredTransactions " +
            ":transaction_type, " +
            ":status, " +
            ":user_id",
            nativeQuery = true)
    List<Transaction> getFilteredTransactions(
            @Param("transaction_type") String transactionType,
            @Param("status") String status,
            @Param("user_id") UUID user_id
    );

}









