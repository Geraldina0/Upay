package com.example.demo.service;

import com.example.demo.Dto.NotificationRequest;
import com.example.demo.Dto.NotificationResponse;
import com.example.demo.models.Notification;
import com.example.demo.models.User;
import com.example.demo.repository.NotificationRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.websocket.CustomWebSocketHandler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final CustomWebSocketHandler customWebSocketHandler;

    public NotificationService(NotificationRepository notificationRepository,
                               UserRepository userRepository,
                               CustomWebSocketHandler customWebSocketHandler) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
        this.customWebSocketHandler = customWebSocketHandler;
    }

    public NotificationResponse createNotification(NotificationRequest request) {
        User user = userRepository.findByEmail(request.getReceiverEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Notification notification = new Notification(user, request.getType(), request.getMessage());
        Notification saved = notificationRepository.save(notification);

        String wsPayload = buildWebSocketPayload(saved);
        customWebSocketHandler.sendToUser(user.getEmail(), wsPayload);

        return toResponse(saved, user.getName(), getUnreadCount(user.getEmail()));
    }

    public NotificationResponse createNotification(User user, String message, com.example.demo.models.NotificationType type) {
        Notification notification = new Notification(user, type, message);
        Notification saved = notificationRepository.save(notification);

        String wsPayload = buildWebSocketPayload(saved);
        customWebSocketHandler.sendToUser(user.getEmail(), wsPayload);

        return toResponse(saved, user.getName(), getUnreadCount(user.getEmail()));
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> getUserNotifications(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        long unreadCount = getUnreadCount(email);

        return notificationRepository.findByUser_Email(email)
                .stream()
                .map(notification -> toResponse(notification, user.getName(), unreadCount))
                .toList();
    }

    @Transactional(readOnly = true)
    public long getUnreadCount(String email) {
        return notificationRepository.countByUser_EmailAndIsReadFalse(email);
    }

    public void markAsRead(String email, UUID notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));

        if (!notification.getUser().getEmail().equals(email)) {
            throw new RuntimeException("You are not allowed to update this notification");
        }

        if (!notification.isRead()) {
            notification.setRead(true);
            notificationRepository.save(notification);
        }
    }

    public void markAllAsRead(String email) {
        List<Notification> notifications = notificationRepository.findByUser_Email(email);

        notifications.forEach(notification -> {
            if (!notification.isRead()) {
                notification.setRead(true);
            }
        });

        notificationRepository.saveAll(notifications);
    }

    @Transactional(readOnly = true)
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    private NotificationResponse toResponse(Notification notification, String userName, long unreadCount) {
        return new NotificationResponse(
                notification.getId(),
                notification.getMessage(),
                notification.getType(),
                notification.getCreatedAt(),
                notification.isRead(),
                userName,
                unreadCount
        );
    }

    private String buildWebSocketPayload(Notification notification) {
        return "{"
                + "\"id\":\"" + notification.getId() + "\","
                + "\"message\":\"" + escapeJson(notification.getMessage()) + "\","
                + "\"type\":\"" + notification.getType() + "\","
                + "\"createdAt\":\"" + notification.getCreatedAt() + "\","
                + "\"read\":" + notification.isRead()
                + "}";
    }

    private String escapeJson(String value) {
        return value.replace("\"", "\\\"");
    }
}