package com.example.demo.controller;

import com.example.demo.Dto.UserRequest;
import com.example.demo.Dto.UserResponse;
import com.example.demo.models.User;
import com.example.demo.repository.RoleRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.UserService;
import com.example.demo.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.*;
import org.springframework.web.bind.annotation.CrossOrigin;

@RestController
@RequestMapping("/user")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;

    @Autowired
    public UserController(UserService userService, JwtUtil jwtUtil, RoleRepository roleRepository, UserRepository userRepository) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
    }

    @PostMapping("/register/admin")
    public ResponseEntity<?> registerAdmin(@RequestBody UserRequest userRequest) {
        try {
            UserResponse response = userService.register(userRequest, "ADMIN");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/register/user")
    public ResponseEntity<?> registerUser(@RequestBody UserRequest userRequest) {
        try {
            UserResponse response = userService.register(userRequest, "USER");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }


@PreAuthorize("hasRole('ADMIN')")
@GetMapping("/all")
public ResponseEntity<?> getAllUsers(@RequestHeader("Authorization") String token) {
    try {
        if (!userService.isAdmin(token)) {
            return ResponseEntity.status(403).body(Map.of("error", "Access denied. Admins only."));
        }

        List<User> users = userService.getAllUsers(); // No more role as a parameter
        return ResponseEntity.ok(users);
    } catch (RuntimeException e) {
        return ResponseEntity.status(401).body(Map.of("error", e.getMessage()));
    }
}



    @GetMapping("/token")
    public ResponseEntity<Object> getUserData(@RequestHeader("Authorization") String token) {
        if (token.startsWith("Bearer ")) {
            token = token.substring(7); // Remove "Bearer " part of the token
        }

        if (jwtUtil.isTokenExpired(token)) {
            // Token is expired
            logger.warn("Token has expired");
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Token has expired");
            return ResponseEntity.status(401).body(errorResponse);
        }

        // Token is active, extract user info
        String email = jwtUtil.extractEmail(token);
        Optional<UserResponse> user = userService.findByEmail(email);

        return user.<ResponseEntity<Object>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(404).body(Map.of("error", "User not found")));
    }

}
