package com.example.demo.service;

import com.example.demo.models.Friends;
import com.example.demo.models.NotificationType;
import com.example.demo.models.User;
import com.example.demo.repository.FriendsRepository;
import com.example.demo.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class FriendsService {

    private static final Logger logger = LoggerFactory.getLogger(FriendsService.class);

    private final UserRepository userRepository;
    private final FriendsRepository friendsRepository;
    private final NotificationService notificationService;

    public FriendsService(UserRepository userRepository,
                          FriendsRepository friendsRepository,
                          NotificationService notificationService) {
        this.userRepository = userRepository;
        this.friendsRepository = friendsRepository;
        this.notificationService = notificationService;
    }

    @Transactional
    public Friends sendFriendRequest(String email, UUID receiverId) {
        logger.info("Processing friend request - Receiver: {}", receiverId);

        User sender = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Sender not found"));

        User receiver = userRepository.findById(receiverId)
                .orElseThrow(() -> new RuntimeException("Receiver not found"));

        if (sender.getId().equals(receiverId)) {
            throw new RuntimeException("You cannot send a friend request to yourself");
        }

        boolean alreadyExists = friendsRepository.findByReceiverIdAndStatus(receiverId, Friends.Status.PENDING)
                .stream()
                .anyMatch(f -> f.getSenderId().equals(sender.getId()));

        if (alreadyExists) {
            throw new RuntimeException("Friend request already sent");
        }

        Friends friendRequest = new Friends();
        friendRequest.setSenderId(sender.getId());
        friendRequest.setReceiverId(receiverId);
        friendRequest.setStatus(Friends.Status.PENDING);

        Friends savedFriendRequest = friendsRepository.save(friendRequest);
        logger.info("Friend request saved - ID: {}", savedFriendRequest.getId());

        String notificationMessage = String.format(
                "%s sent you a friend request",
                sender.getEmail()
        );

        notificationService.createNotification(
                receiver,
                notificationMessage,
                NotificationType.FRIEND_REQUEST
        );

        logger.info("Notification created for user {}: {}", receiver.getEmail(), notificationMessage);

        return savedFriendRequest;
    }

    public List<Friends> getPendingRequests(UUID userId) {
        return friendsRepository.findByReceiverIdAndStatus(userId, Friends.Status.PENDING);
    }

    @Transactional
    public Friends updateRequestStatus(UUID requestId, Friends.Status status, UUID userId) {
        Friends friendRequest = friendsRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Friend request not found"));

        if (!friendRequest.getReceiverId().equals(userId)) {
            throw new RuntimeException("Only the receiver can update this request");
        }

        friendRequest.setStatus(status);
        Friends updatedRequest = friendsRepository.save(friendRequest);

        if (status == Friends.Status.ACCEPTED) {
            User sender = userRepository.findById(friendRequest.getSenderId())
                    .orElseThrow(() -> new RuntimeException("Sender not found"));

            User receiver = userRepository.findById(friendRequest.getReceiverId())
                    .orElseThrow(() -> new RuntimeException("Receiver not found"));

            String notificationMessage = String.format(
                    "%s accepted your friend request",
                    receiver.getEmail()
            );

            notificationService.createNotification(
                    sender,
                    notificationMessage,
                    NotificationType.FRIEND_ACCEPTED
            );
        }

        return updatedRequest;
    }
}