package com.example.demo.models;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "friends")
public class Friends {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(nullable = false)
    private UUID senderId;  // Ensure senderId is a UUID

    @Column(nullable = false)
    private UUID receiverId;

    @Column(name = "request_date", nullable = false)
    private LocalDate requestDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;


    public Friends() {
        this.requestDate = LocalDate.now();
        this.status = Status.PENDING;
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public UUID getSenderId() { return senderId; }
    public UUID getReceiverId() { return receiverId; }
    public LocalDate getDate() { return requestDate; }
    public Status getStatus() { return status; }

    public void setSenderId(UUID senderId) { this.senderId = senderId; }  // Ensure senderId is set
    public void setReceiverId(UUID receiverId) { this.receiverId = receiverId; }
    public void setStatus(Status status) { this.status = status; }

    public void setUpdatedAt(LocalDate now) {
    }


    public enum Status {
        PENDING,
        ACCEPTED,
        REJECTED
    }
}
