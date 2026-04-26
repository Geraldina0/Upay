package com.example.demo.service;

import com.example.demo.Dto.TransactionRequest;
import com.example.demo.models.*;
import com.example.demo.repository.FriendsRepository;
import com.example.demo.repository.TransactionRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.WalletRepository;
import com.example.demo.util.JwtUtil;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class TransactionService {

    private static final Logger logger = LoggerFactory.getLogger(TransactionService.class);

    private final TransactionRepository transactionRepository;
    private final WalletRepository walletRepository;
    private final UserRepository userRepository;
    private final FriendsRepository friendsRepository;
    private final JwtUtil jwtUtil;
    private final ExchangeService exchangeService;
    private final NotificationService notificationService;

    public TransactionService(TransactionRepository transactionRepository,
                              WalletRepository walletRepository,
                              UserRepository userRepository,
                              FriendsRepository friendsRepository,
                              JwtUtil jwtUtil,
                              NotificationService notificationService,
                              ExchangeService exchangeService) {
        this.transactionRepository = transactionRepository;
        this.walletRepository = walletRepository;
        this.userRepository = userRepository;
        this.friendsRepository = friendsRepository;
        this.jwtUtil = jwtUtil;
        this.notificationService = notificationService;
        this.exchangeService = exchangeService;
    }

    @Transactional
    public Transaction transfer(String token, UUID senderWalletId, UUID receiverWalletId, BigDecimal amountTransferred) {
        logger.info("Processing transfer - Sender wallet: {}, Receiver wallet: {}, Amount: {}",
                senderWalletId, receiverWalletId, amountTransferred);

        validateAmount(amountTransferred);

        String email = jwtUtil.extractEmail(token);

        User loggedInUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Authenticated user not found."));

        Wallet senderWallet = walletRepository.findById(senderWalletId)
                .orElseThrow(() -> new RuntimeException("Sender wallet not found."));

        Wallet receiverWallet = walletRepository.findById(receiverWalletId)
                .orElseThrow(() -> new RuntimeException("Receiver wallet not found."));

        if (!senderWallet.getUser().getId().equals(loggedInUser.getId())) {
            throw new RuntimeException("Unauthorized: You can only transfer money from your own wallet.");
        }

        if (!Boolean.TRUE.equals(senderWallet.getIsActive())) {
            throw new RuntimeException("Sender wallet is inactive.");
        }

        if (!Boolean.TRUE.equals(receiverWallet.getIsActive())) {
            throw new RuntimeException("Receiver wallet is inactive.");
        }

        if (senderWallet.getId().equals(receiverWallet.getId())) {
            throw new RuntimeException("You cannot transfer money to the same wallet.");
        }

        User sender = senderWallet.getUser();
        User receiver = receiverWallet.getUser();

        if (sender == null || receiver == null) {
            throw new RuntimeException("Wallet user not found.");
        }

        if (senderWallet.getBalance().compareTo(amountTransferred) < 0) {
            throw new RuntimeException("Insufficient funds.");
        }

        if (!isValidTransfer(sender, receiver)) {
            throw new RuntimeException("You must be friends to transfer money.");
        }

        BigDecimal exchangeRateValue = BigDecimal.valueOf(
                exchangeService.getLatestRate(senderWallet.getCurrency(), receiverWallet.getCurrency())
        );

        BigDecimal convertedAmount = amountTransferred.multiply(exchangeRateValue);

        // sender wallet audit fields
        senderWallet.setPreviousBalance(senderWallet.getBalance());
        senderWallet.setTransactionAmount(amountTransferred);

        // receiver wallet audit fields
        receiverWallet.setPreviousBalance(receiverWallet.getBalance());
        receiverWallet.setTransactionAmount(convertedAmount);

        senderWallet.setBalance(senderWallet.getBalance().subtract(amountTransferred));
        receiverWallet.setBalance(receiverWallet.getBalance().add(convertedAmount));

        walletRepository.save(senderWallet);
        walletRepository.save(receiverWallet);

        Transaction transaction = new Transaction();
        transaction.setWalletOut(senderWallet);
        transaction.setWalletIn(receiverWallet);
        transaction.setTransactionId(UUID.randomUUID());
        transaction.setTransactionType(TransactionType.TRANSFER);
        transaction.setAmountTransferred(amountTransferred);
        transaction.setExchangeRate(exchangeRateValue);
        transaction.setStatus(TransactionStatus.COMPLETED);
        transaction.setDate(LocalDate.now());

        Transaction savedTransaction = transactionRepository.save(transaction);

        String receiverMessage = String.format(
                "You received %s %s from %s",
                convertedAmount.toPlainString(),
                receiverWallet.getCurrency().getName(),
                sender.getEmail()
        );

        notificationService.createNotification(
                receiver,
                receiverMessage,
                NotificationType.MONEY_RECEIVED
        );

        String senderMessage = String.format(
                "You sent %s %s to %s",
                amountTransferred.toPlainString(),
                senderWallet.getCurrency().getName(),
                receiver.getEmail()
        );

        notificationService.createNotification(
                sender,
                senderMessage,
                NotificationType.TRANSACTION_SENT
        );

        logger.info("Transfer completed successfully. Transaction ID: {}", savedTransaction.getTransactionId());

        return savedTransaction;
    }

    @Transactional
    public Transaction deposit(UUID userWalletId, BigDecimal amount, String token) {
        logger.info("Processing deposit - User wallet: {}, Amount: {}", userWalletId, amount);

        validateAmount(amount);

        String email = jwtUtil.extractEmail(token);

        User loggedInUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Authenticated user not found."));

        Wallet userWallet = walletRepository.findById(userWalletId)
                .orElseThrow(() -> new RuntimeException("User wallet not found."));

        if (!Boolean.TRUE.equals(userWallet.getIsActive())) {
            throw new RuntimeException("User wallet is inactive.");
        }

        boolean isAdmin = loggedInUser.getRole() != null
                && "ADMIN".equalsIgnoreCase(loggedInUser.getRole().getName());

        if (!isAdmin && !userWallet.getUser().getId().equals(loggedInUser.getId())) {
            throw new RuntimeException("Unauthorized: You can only deposit into your own wallet.");
        }

        userWallet.setPreviousBalance(userWallet.getBalance());
        userWallet.setTransactionAmount(amount);
        userWallet.setBalance(userWallet.getBalance().add(amount));

        walletRepository.save(userWallet);

        Transaction transaction = new Transaction();
        transaction.setWalletOut(null);
        transaction.setWalletIn(userWallet);
        transaction.setTransactionId(UUID.randomUUID());
        transaction.setTransactionType(TransactionType.DEPOSIT);
        transaction.setAmountTransferred(amount);
        transaction.setExchangeRate(BigDecimal.ONE);
        transaction.setStatus(TransactionStatus.COMPLETED);
        transaction.setDate(LocalDate.now());

        Transaction savedTransaction = transactionRepository.save(transaction);

        String notificationMessage = String.format(
                "A deposit of %s %s was added to your wallet",
                amount.toPlainString(),
                userWallet.getCurrency().getName()
        );

        notificationService.createNotification(
                userWallet.getUser(),
                notificationMessage,
                NotificationType.BALANCE_ADDED
        );

        logger.info("Deposit completed successfully. Transaction ID: {}", savedTransaction.getTransactionId());

        return savedTransaction;
    }

    @Transactional
    public Transaction withdraw(UUID userWalletId, BigDecimal amount, String token) {
        logger.info("Processing withdraw - User wallet: {}, Amount: {}", userWalletId, amount);

        validateAmount(amount);

        String email = jwtUtil.extractEmail(token);

        User loggedInUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Authenticated user not found."));

        Wallet userWallet = walletRepository.findById(userWalletId)
                .orElseThrow(() -> new RuntimeException("User wallet not found."));

        if (!Boolean.TRUE.equals(userWallet.getIsActive())) {
            throw new RuntimeException("User wallet is inactive.");
        }

        boolean isAdmin = loggedInUser.getRole() != null
                && "ADMIN".equalsIgnoreCase(loggedInUser.getRole().getName());

        if (!isAdmin && !userWallet.getUser().getId().equals(loggedInUser.getId())) {
            throw new RuntimeException("Unauthorized: You can only withdraw from your own wallet.");
        }

        if (userWallet.getBalance().compareTo(amount) < 0) {
            throw new RuntimeException("Insufficient funds in user wallet.");
        }

        userWallet.setPreviousBalance(userWallet.getBalance());
        userWallet.setTransactionAmount(amount);
        userWallet.setBalance(userWallet.getBalance().subtract(amount));

        walletRepository.save(userWallet);

        Transaction transaction = new Transaction();
        transaction.setWalletOut(userWallet);
        transaction.setWalletIn(null);
        transaction.setTransactionId(UUID.randomUUID());
        transaction.setTransactionType(TransactionType.WITHDRAW);
        transaction.setAmountTransferred(amount);
        transaction.setExchangeRate(BigDecimal.ONE);
        transaction.setStatus(TransactionStatus.COMPLETED);
        transaction.setDate(LocalDate.now());

        Transaction savedTransaction = transactionRepository.save(transaction);

        String notificationMessage = String.format(
                "A withdrawal of %s %s was made from your wallet",
                amount.toPlainString(),
                userWallet.getCurrency().getName()
        );

        notificationService.createNotification(
                userWallet.getUser(),
                notificationMessage,
                NotificationType.WITHDRAWAL
        );

        logger.info("Withdraw completed successfully. Transaction ID: {}", savedTransaction.getTransactionId());

        return savedTransaction;
    }

    public List<Transaction> getTransactions(String token, TransactionRequest request) {
        String email = jwtUtil.extractEmail(token);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));

        return transactionRepository.getFilteredTransactions(
                request.getTransactionType(),
                request.getStatus(),
                user.getId()
        );
    }

    private void validateAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Amount must be greater than zero.");
        }
    }

    private boolean isValidTransfer(User sender, User receiver) {
        List<Friends> friendships1 = friendsRepository.findByReceiverIdAndStatus(
                receiver.getId(),
                Friends.Status.ACCEPTED
        );

        boolean senderToReceiver = friendships1.stream()
                .anyMatch(f -> f.getSenderId().equals(sender.getId()));

        List<Friends> friendships2 = friendsRepository.findByReceiverIdAndStatus(
                sender.getId(),
                Friends.Status.ACCEPTED
        );

        boolean receiverToSender = friendships2.stream()
                .anyMatch(f -> f.getSenderId().equals(receiver.getId()));

        return senderToReceiver || receiverToSender;
    }
}