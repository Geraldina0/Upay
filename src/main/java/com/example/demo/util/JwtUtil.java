package com.example.demo.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

@Component
public class JwtUtil {

    private static final String SECRET_KEY =
            "ijfhuzsfhgawuogfawirbgaeuorhgouawergboauwerhgeurbgoergowieurhyurutgeyfhsdiojgfndbgakldugfnosufbgsdjkgndufhgbsdufbgyoaeubaeu";

    private static final long ACCESS_EXPIRATION_TIME = 1000 * 60 * 60 * 2;
    private static final long REFRESH_EXPIRATION_TIME = 1000L * 60 * 60 * 24 * 7;

    private final Key key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes());

    public String generateToken(String email) {
        return createToken(email, ACCESS_EXPIRATION_TIME, "ACCESS");
    }

    public String generateRefreshToken(String email) {
        return createToken(email, REFRESH_EXPIRATION_TIME, "REFRESH");
    }

    private String createToken(String email, long expirationTime, String tokenType) {
        return Jwts.builder()
                .setSubject(email)
                .claim("tokenType", tokenType)
                .setId(UUID.randomUUID().toString())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String extractEmail(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public String extractTokenType(String token) {
        return getClaimFromJwtToken(token).get("tokenType", String.class);
    }

    public boolean isTokenExpired(String token) {
        try {
            Date expirationDate = getClaimFromJwtToken(token).getExpiration();
            return expirationDate.before(new Date());
        } catch (ExpiredJwtException e) {
            return true;
        }
    }

    private Claims getClaimFromJwtToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public Map<String, Object> getAllClaimsFromJwtToken(String token) {
        return getClaimFromJwtToken(token);
    }
}