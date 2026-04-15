package com.example.demo.service;

import com.example.demo.models.Otp;
import com.example.demo.repository.OtpRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;

@Service
public class OtpService {

    private final OtpRepository otpRepository;

    public OtpService(OtpRepository otpRepository) {
        this.otpRepository = otpRepository;
    }

    public String generateOtp(String email) {
        String otpCode = String.format("%06d", new Random().nextInt(999999));
        LocalDateTime expiry = LocalDateTime.now().plusMinutes(5);

        Otp otp = new Otp();
        otp.setEmail(email);
        otp.setOtp(otpCode);
        otp.setExpiryTime(expiry);
        otp.setUsed(false);
        otpRepository.save(otp);

        // In dev mode: return OTP so the frontend can show it
        return otpCode;
    }

    public boolean validateOtp(String email, String otpCode) {
        return otpRepository.findByEmailAndOtpAndUsedFalse(email, otpCode)
                .filter(otp -> otp.getExpiryTime().isAfter(LocalDateTime.now()))
                .map(validOtp -> {
                    validOtp.setUsed(true);
                    otpRepository.save(validOtp);
                    return true;
                })
                .orElse(false);
    }
    }





