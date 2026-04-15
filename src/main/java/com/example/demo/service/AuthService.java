package com.example.demo.service;

import com.example.demo.Dto.AuthResponse;
import com.example.demo.models.AuthToken;
import com.example.demo.models.User;
import com.example.demo.repository.AuthTokenRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final OtpService otpService;
    private final AuthTokenRepository authTokenRepository;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil,
                       OtpService otpService,
                       AuthTokenRepository authTokenRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.otpService = otpService;
        this.authTokenRepository = authTokenRepository;
    }

    public String authenticate(String email, String password, String role) {
        logger.info("Authenticating user: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Invalid credentials"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }

        if ("ADMIN".equals(role) &&
                (user.getRole() == null || !user.getRole().getName().equals("ADMIN"))) {
            throw new RuntimeException("Access denied: Admin role required");
        }

        String otp = otpService.generateOtp(email);
        logger.info("OTP generated for {}: {}", email, otp);

        return otp;
    }

    @Transactional
    public AuthResponse verifyOtp(String email, String otpCode) {
        if (!otpService.validateOtp(email, otpCode)) {
            throw new RuntimeException("Invalid or expired OTP");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String accessToken = jwtUtil.generateToken(email);
        String refreshToken = jwtUtil.generateRefreshToken(email);

        AuthToken authToken = authTokenRepository.findByUser_Id(user.getId())
                .orElse(new AuthToken());

        authToken.setUser(user);
        authToken.setAccessToken(accessToken);
        authToken.setRefreshToken(refreshToken);
        authToken.setLoggedOut(false);

        authTokenRepository.save(authToken);

        return new AuthResponse(email, accessToken, refreshToken);
    }

    public boolean isTokenExpired(String token) {
        return jwtUtil.isTokenExpired(token);
    }

    public boolean isAccessTokenValid(String token) {
        if (jwtUtil.isTokenExpired(token)) {
            return false;
        }

        return authTokenRepository.findByAccessToken(token)
                .map(storedToken -> !storedToken.isLoggedOut())
                .orElse(false);
    }

    public boolean isRefreshTokenValid(String token) {
        if (jwtUtil.isTokenExpired(token)) {
            return false;
        }

        return authTokenRepository.findByRefreshToken(token)
                .map(storedToken -> !storedToken.isLoggedOut())
                .orElse(false);
    }

    @Transactional
    public void logout(String accessToken) {
        AuthToken storedToken = authTokenRepository.findByAccessToken(accessToken)
                .orElseThrow(() -> new RuntimeException("Access token not found"));

        storedToken.setLoggedOut(true);
        authTokenRepository.save(storedToken);
    }
}