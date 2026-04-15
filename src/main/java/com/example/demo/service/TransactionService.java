package com.example.demo.service;

import com.example.demo.Dto.OtpResponse;
import com.example.demo.Dto.TransactionRequest;
import com.example.demo.models.*;
import com.example.demo.repository.ExchangeRepository;
import com.example.demo.repository.FriendsRepository;
import com.example.demo.repository.TransactionRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.WalletRepository;
import com.example.demo.util.JwtUtil;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.sql.Date;
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
    private final OtpService otpService;
    private final ExchangeService exchangeService;
    private final ExchangeRepository exchangeRepository;
    private final NotificationService notificationService;

    public TransactionService(TransactionRepository transactionRepository,
                              WalletRepository walletRepository,
                              UserRepository userRepository,
                              FriendsRepository friendsRepository,
                              JwtUtil jwtUtil,
                              OtpService otpService,
                              NotificationService notificationService,
                              ExchangeService exchangeService,
                              ExchangeRepository exchangeRepository) {
        this.transactionRepository = transactionRepository;
        this.walletRepository = walletRepository;
        this.userRepository = userRepository;
        this.friendsRepository = friendsRepository;
        this.jwtUtil = jwtUtil;
        this.otpService = otpService;
        this.notificationService = notificationService;
        this.exchangeService = exchangeService;
        this.exchangeRepository = exchangeRepository;
    }

    @Transactional
    public Transaction transfer(UUID senderWalletId, UUID receiverWalletId, Double amountTransferred) {
        logger.info("Processing transfer - Sender wallet: {}, Receiver wallet: {}, Amount: {}",
                senderWalletId, receiverWalletId, amountTransferred);

        if (amountTransferred == null || amountTransferred <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero.");
        }

        Wallet senderWallet = walletRepository.findById(senderWalletId)
                .orElseThrow(() -> new IllegalArgumentException("Sender wallet not found"));

        Wallet receiverWallet = walletRepository.findById(receiverWalletId)
                .orElseThrow(() -> new IllegalArgumentException("Receiver wallet not found"));

        User sender = senderWallet.getUser();
        User receiver = receiverWallet.getUser();

        if (sender == null || receiver == null) {
            throw new IllegalArgumentException("User associated with wallet not found.");
        }

        if (senderWallet.getBalance() < amountTransferred) {
            throw new IllegalArgumentException("Insufficient funds.");
        }

        if (!isValidTransfer(sender, receiver)) {
            throw new IllegalArgumentException("You must be friends.");
        }

        double exchangeRateValue = exchangeService.getLatestRate(
                senderWallet.getCurrency(),
                receiverWallet.getCurrency()
        );

        senderWallet.setBalance(senderWallet.getBalance() - amountTransferred);

        double convertedAmount = amountTransferred * exchangeRateValue;
        receiverWallet.setBalance(receiverWallet.getBalance() + convertedAmount);

        walletRepository.save(senderWallet);
        walletRepository.save(receiverWallet);

        Exchange exchangeRate = exchangeRepository.findByFromCurrencyAndToCurrency(
                senderWallet.getCurrency(),
                receiverWallet.getCurrency()
        ).orElseThrow(() -> new RuntimeException("Exchange rate not found"));

        Transaction transaction = new Transaction();
        transaction.setWalletIn(receiverWallet);
        transaction.setWalletOut(senderWallet);
        transaction.setTransactionId(UUID.randomUUID());
        transaction.setTransactionType(TransactionType.TRANSFER);
        transaction.setAmount_transferred(amountTransferred);
        transaction.setStatus(TransactionStatus.PENDING);
        transaction.setDate(Date.valueOf(LocalDate.now()).toLocalDate());

        Transaction savedTransaction = transactionRepository.save(transaction);

        String receiverMessage = String.format(
                "You received %.2f from %s",
                convertedAmount,
                sender.getEmail()
        );

        notificationService.createNotification(
                receiver,
                receiverMessage,
                NotificationType.MONEY_RECEIVED
        );

        String senderMessage = String.format(
                "You sent %.2f to %s",
                amountTransferred,
                receiver.getEmail()
        );

        notificationService.createNotification(
                sender,
                senderMessage,
                NotificationType.TRANSACTION_SENT
        );

        logger.info("Transaction successful - ID: {}, Amount: {}, Status: {}, Date: {}",
                savedTransaction.getTransactionId(),
                savedTransaction.getAmount_transferred(),
                savedTransaction.getStatus(),
                savedTransaction.getDate());

        logger.info("Notifications created for sender {} and receiver {}",
                sender.getEmail(), receiver.getEmail());

        return savedTransaction;
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

    @Transactional
    public void deposit(UUID adminWalletId, UUID userWalletId, double amount, String token) {
        String email = jwtUtil.extractEmail(token);

        User adminUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Admin user not found"));

        if (!adminUser.getRole().getName().equals("ADMIN")) {
            throw new RuntimeException("Unauthorized: Only ADMIN users can perform transactions.");
        }

        Wallet adminWallet = walletRepository.findById(adminWalletId)
                .orElseThrow(() -> new RuntimeException("Admin wallet not found"));

        if (!adminWallet.getUser().getId().equals(adminUser.getId())) {
            throw new RuntimeException("Unauthorized: Wallet does not belong to the logged-in admin.");
        }

        Wallet userWallet = walletRepository.findById(userWalletId)
                .orElseThrow(() -> new RuntimeException("User wallet not found"));

        double prevAdminBalance = adminWallet.getBalance();
        double prevUserBalance = userWallet.getBalance();

        userWallet.setPreviousBalance(prevUserBalance);
        userWallet.setTransactionAmount(amount);

        adminWallet.setBalance(prevAdminBalance - amount);
        userWallet.setBalance(prevUserBalance + amount);

        walletRepository.save(adminWallet);
        walletRepository.save(userWallet);

        String logMessage = String.format(
                "Deposit successful! Admin (Wallet ID: %s): Previous Balance: %.2f, New Balance: %.2f | User (Wallet ID: %s): Previous Balance: %.2f, New Balance: %.2f",
                adminWalletId, prevAdminBalance, adminWallet.getBalance(),
                userWalletId, prevUserBalance, userWallet.getBalance()
        );

        logger.info(logMessage);

        Transaction transaction = new Transaction();
        transaction.setWalletIn(adminWallet);
        transaction.setWalletOut(userWallet);
        transaction.setAmount_transferred(amount);
        transaction.setType(TransactionType.DEPOSIT);
        transaction.setDate(Date.valueOf(LocalDate.now()).toLocalDate());
        transaction.setStatus(TransactionStatus.COMPLETED);

        transactionRepository.save(transaction);

        String notificationMessage = String.format(
                "%s deposited %.2f into your wallet",
                adminUser.getEmail(),
                amount
        );

        notificationService.createNotification(
                userWallet.getUser(),
                notificationMessage,
                NotificationType.BALANCE_ADDED
        );
    }

    @Transactional
    public void withdraw(UUID adminWalletId, UUID userWalletId, double amount, String token) {
        String email = jwtUtil.extractEmail(token);

        User adminUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Admin user not found"));

        if (!adminUser.getRole().getName().equals("ADMIN")) {
            throw new RuntimeException("Unauthorized: Only ADMIN users can perform transactions.");
        }

        Wallet adminWallet = walletRepository.findById(adminWalletId)
                .orElseThrow(() -> new RuntimeException("Admin wallet not found"));

        if (!adminWallet.getUser().getId().equals(adminUser.getId())) {
            throw new RuntimeException("Unauthorized: Wallet does not belong to the logged-in admin.");
        }

        Wallet userWallet = walletRepository.findById(userWalletId)
                .orElseThrow(() -> new RuntimeException("User wallet not found"));

        double prevUserBalance = userWallet.getBalance();

        userWallet.setPreviousBalance(prevUserBalance);
        userWallet.setTransactionAmount(-amount);

        if (prevUserBalance < amount) {
            throw new RuntimeException("Insufficient funds in user wallet.");
        }

        userWallet.setBalance(prevUserBalance - amount);
        walletRepository.save(userWallet);

        String logMessage = String.format(
                "Withdrawal successful! User (Wallet ID: %s): Previous Balance: %.2f, New Balance: %.2f",
                userWalletId, prevUserBalance, userWallet.getBalance()
        );

        logger.info(logMessage);

        Transaction transaction = new Transaction();
        transaction.setWalletIn(adminWallet);
        transaction.setWalletOut(userWallet);
        transaction.setAmount_transferred(amount);
        transaction.setType(TransactionType.WITHDRAW);
        transaction.setDate(Date.valueOf(LocalDate.now()).toLocalDate());
        transaction.setStatus(TransactionStatus.COMPLETED);

        transactionRepository.save(transaction);

        String notificationMessage = String.format(
                "%s withdrew %.2f from your wallet",
                adminUser.getEmail(),
                amount
        );

        notificationService.createNotification(
                userWallet.getUser(),
                notificationMessage,
                NotificationType.WITHDRAWAL
        );
    }

    public OtpResponse verifyOtp(String email, String otp, UUID transactionId) {
        if (!otpService.validateOtp(email, otp)) {
            throw new RuntimeException("Invalid or expired OTP");
        }

        return new OtpResponse(email, otp, transactionId);
    }
}