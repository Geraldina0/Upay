package com.example.demo.controller;

import com.example.demo.Dto.AuthResponse;
import com.example.demo.Dto.LoginRequest;
import com.example.demo.service.AuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login/user")
    public ResponseEntity<?> userLogin(@RequestBody LoginRequest loginRequest) {
        try {
            String otp = authService.authenticate(loginRequest.getEmail(), loginRequest.getPassword(), "USER");
            return ResponseEntity.ok().body("OTP for testing: " + otp);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }

    @PostMapping("/login/admins")
    public ResponseEntity<?> adminLogin(@RequestBody LoginRequest loginRequest) {
        try {
            String otp = authService.authenticate(loginRequest.getEmail(), loginRequest.getPassword(), "ADMIN");
            return ResponseEntity.ok().body("OTP for testing: " + otp);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@RequestParam String email, @RequestParam String otp) {
        try {
            AuthResponse response = authService.verifyOtp(email, otp);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @GetMapping("/test")
    public ResponseEntity<String> testToken(@RequestHeader("Authorization") String token) {
        String tokenWithoutBearer = token.startsWith("Bearer ") ? token.substring(7) : token;

        if (authService.isAccessTokenValid(tokenWithoutBearer)) {
            return ResponseEntity.ok("Access token is active");
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Access token is invalid or expired");
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader("Authorization") String authorizationHeader) {
        try {
            if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid or missing token");
            }

            String accessToken = authorizationHeader.substring(7).trim();
            authService.logout(accessToken);

            return ResponseEntity.ok("Logged out successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}