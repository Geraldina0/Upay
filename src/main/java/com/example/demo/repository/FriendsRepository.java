package com.example.demo.repository;

import com.example.demo.models.Friends;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.query.Procedure;

import java.util.List;
import java.util.UUID;

public interface FriendsRepository extends JpaRepository<Friends, UUID> {
    List<Friends> findByReceiverIdAndStatus(UUID receiverId, Friends.Status status);

    @Procedure(procedureName = "InsertFriendRequest")
    void insertFriendRequest(UUID senderId, UUID receiverId, String status);

}
