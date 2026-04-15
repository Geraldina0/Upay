package com.example.demo.controller;

import com.example.demo.models.Friends;
import com.example.demo.models.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.FriendsService;
import com.example.demo.util.JwtUtil;
import com.example.demo.websocket.CustomWebSocketHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/friends")
public class FriendsController {

    private final FriendsService friendService;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private static final Logger logger = LoggerFactory.getLogger(FriendsController.class);
    private final CustomWebSocketHandler customWebSocketHandler;

    public FriendsController(FriendsService friendService, JwtUtil jwtUtil, UserRepository userRepository, CustomWebSocketHandler customWebSocketHandler) {
        this.friendService = friendService;
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
        this.customWebSocketHandler = customWebSocketHandler;

    }

    @PostMapping("/send")
    public ResponseEntity<?> sendRequest(
            @RequestHeader("Authorization") String authorizationHeader,
            @RequestParam("receiverId") UUID receiverId) {
        try {
            if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid or missing token");
            }

            String token = authorizationHeader.substring(7).trim();
            String senderEmail = jwtUtil.extractEmail(token);

            logger.info("Sending friend request from {}", senderEmail);

            Friends friendRequest = friendService.sendFriendRequest(
                    senderEmail,
                    receiverId
            );


            return ResponseEntity.status(HttpStatus.CREATED).body(friendRequest);
        } catch (RuntimeException e) {
            logger.error("Failed to send friend request: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error sending friend request: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An unexpected error occurred: " + e.getMessage());
        }
    }

    @GetMapping("/pending")
    public ResponseEntity<List<Friends>> getPendingRequests(@RequestHeader("Authorization") String authorizationHeader) {
        try {
            if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
            }

            String token = authorizationHeader.substring(7).trim();
            String email = jwtUtil.extractEmail(token);
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found with email: " + email));

            List<Friends> pendingRequests = friendService.getPendingRequests(user.getId());
            return ResponseEntity.ok(pendingRequests);
        } catch (RuntimeException e) {
            logger.error("Failed to get pending requests: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } catch (Exception e) {
            logger.error("Unexpected error getting pending requests: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @PostMapping("/accept/{requestId}")
    public ResponseEntity<?> acceptRequest(
            @PathVariable("requestId") UUID requestId,
            @RequestHeader("Authorization") String authorizationHeader) {
        try {
            if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid or missing token");
            }

            String token = authorizationHeader.substring(7).trim();
            String email = jwtUtil.extractEmail(token);
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found with email: " + email));

            Friends updatedRequest = friendService.updateRequestStatus(requestId, Friends.Status.ACCEPTED, user.getId());
            return ResponseEntity.ok(updatedRequest);
        } catch (RuntimeException e) {
            logger.error("Failed to accept friend request: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error accepting friend request: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred: " + e.getMessage());
        }
    }

    @PostMapping("/reject/{requestId}")
    public ResponseEntity<?> rejectRequest(
            @PathVariable("requestId") UUID requestId,
            @RequestHeader("Authorization") String authorizationHeader) {
        try {
            if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid or missing token");
            }

            String token = authorizationHeader.substring(7).trim();
            String email = jwtUtil.extractEmail(token);
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found with email: " + email));

            Friends updatedRequest = friendService.updateRequestStatus(requestId, Friends.Status.REJECTED, user.getId());
            return ResponseEntity.ok(updatedRequest);
        } catch (RuntimeException e) {
            logger.error("Failed to reject friend request: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error rejecting friend request: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred: " + e.getMessage());
        }
    }
}