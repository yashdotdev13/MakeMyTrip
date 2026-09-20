package com.company.MakeMyTrip.api_gateway;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

@Service
public class JwtService {

    @Value("${jwt.secretKey}")
    private String jwtSecretKey;

    private SecretKey getSecretKey() {

        if (jwtSecretKey == null || jwtSecretKey.isBlank()) {
            throw new IllegalStateException(
                    "JWT secret key must not be empty"
            );
        }

        byte[] keyBytes = jwtSecretKey.getBytes(StandardCharsets.UTF_8);

        if (keyBytes.length < 32) {
            throw new IllegalStateException(
                    "JWT secret key must be at least 256 bits (32 bytes)"
            );
        }

        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String getUserIdFromToken(String token) {

        Claims claims = Jwts.parser()
                .verifyWith(getSecretKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();

        Object userIdObj = claims.get("userId");

        if (userIdObj == null) {
            throw new IllegalArgumentException(
                    "Missing userId claim in JWT"
            );
        }

        return userIdObj.toString();
    }
}