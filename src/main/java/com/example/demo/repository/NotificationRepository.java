package com.example.demo.repository;

import com.example.demo.models.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    List<Notification> findByUser_Email(String email);

    long countByUser_EmailAndIsReadFalse(String email);
}


