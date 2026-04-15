// UserService.java
package com.example.demo.service;

import com.example.demo.Dto.UserRequest;
import com.example.demo.Dto.UserResponse;
import com.example.demo.models.User;
import com.example.demo.models.Roles;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.RoleRepository;
import com.example.demo.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class UserService {


    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final WalletService walletService;
    private final JwtUtil jwtUtil;

    public UserService(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder, WalletService walletService, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.walletService = walletService;
        this.jwtUtil = jwtUtil;

    }

    public UserResponse register(UserRequest userRequest, String roleName) {
        logger.info("Starting registration for email: {}", userRequest.getEmail());

        // Check if the user already exists
        if (userRepository.findByEmail(userRequest.getEmail()).isPresent()) {
            logger.warn("Email already exists: {}", userRequest.getEmail());
            throw new RuntimeException("Email already in use. Try another email.");
        }

        // Fetch the role
        Roles role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new RuntimeException("Role not found"));

        // Create the new user
        User user = new User();
        user.setName(userRequest.getName());
        user.setEmail(userRequest.getEmail());
        user.setPassword(passwordEncoder.encode(userRequest.getPassword()));
        user.setRole(role);

        // Save the user
        User savedUser = userRepository.save(user);
        logger.info("User saved successfully with ID: {}", savedUser.getId());


        return new UserResponse(savedUser.getName(), savedUser.getEmail());
    }


    //krijo nje metod qe gjen gjith usersByRole
    //bej nje query tek usersRepository findUserByRole

    public List<User> getAllUsers() {
        Roles userRole = roleRepository.findByName("USER")
                .orElseThrow(() -> new RuntimeException("User role not found"));

        return userRepository.findByRoleId(userRole.getId());
    }

    public boolean isAdmin(String token) {
        if (token.startsWith("Bearer ")) {
            token = token.substring(7); // Remove "Bearer " part of the token
        }

        JwtUtil jwtUtil = new JwtUtil();
        if (jwtUtil.isTokenExpired(token)) {
            throw new RuntimeException("Token has expired");
        }

        String email = jwtUtil.extractEmail(token);
        return userRepository.findByEmail(email)
                .map(user -> "ADMIN".equals(user.getRole().getName()))
                .orElse(false);
    }



    public Optional<UserResponse> findByEmail(String email) {
        return userRepository.findByEmail(email)
                .map(user -> new UserResponse(user.getName(), user.getEmail()));
    }
    // New method to get user from token
    public User getUserFromToken(String token) {
        if (token.startsWith("Bearer ")) {
            token = token.substring(7); // Remove "Bearer " part of the token
        }

        if (jwtUtil.isTokenExpired(token)) {
            throw new RuntimeException("Token has expired");
        }

        String email = jwtUtil.extractEmail(token);
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found for the provided token"));
    }




}
