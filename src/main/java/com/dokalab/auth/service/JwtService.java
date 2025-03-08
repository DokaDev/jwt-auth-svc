package com.dokalab.auth.service;

import com.dokalab.auth.model.*;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class JwtService {

    private final RedisTemplate<String, String> redisTemplate;
    
    // Key prefixes for Redis storage
    private static final String REFRESH_TOKEN_PREFIX = "refresh:";
    private static final String BLACKLIST_PREFIX = "blacklist:";
    
    @Autowired
    public JwtService(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }
    
    /**
     * Generate Access Token
     * @param user User information
     * @return Generated JWT token
     */
    public String generateAccessToken(User user) {
        System.out.println("[JWT] Starting access token generation - User: " + user.getEmail());
        long now = System.currentTimeMillis();
        long expiryTime = now + (JwtConstants.ACCESS_TOKEN_EXPIRY * 1000);
        
        String token = Jwts.builder()
                .setSubject(user.getId())
                .claim("email", user.getEmail())
                .claim("name", user.getName())
                .claim("role", user.getRole())
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(expiryTime))
                .signWith(getAccessTokenSecretKey(), SignatureAlgorithm.HS256)
                .compact();
        
        System.out.println("[JWT] Access token generation complete - Expiry time: " + new Date(expiryTime));
        return token;
    }
    
    /**
     * Generate Refresh Token
     * @param user User information
     * @return Generated Refresh Token
     */
    public String generateRefreshToken(User user) {
        System.out.println("[JWT] Starting refresh token generation - User: " + user.getEmail());
        long now = System.currentTimeMillis();
        long expiryTime = now + (JwtConstants.REFRESH_TOKEN_EXPIRY * 1000);
        
        String refreshToken = Jwts.builder()
                .setSubject(user.getId())
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(expiryTime))
                .signWith(getRefreshTokenSecretKey(), SignatureAlgorithm.HS256)
                .compact();
        
        // Store Refresh Token in Redis
        String key = REFRESH_TOKEN_PREFIX + user.getId();
        redisTemplate.opsForValue().set(key, refreshToken, JwtConstants.REFRESH_TOKEN_EXPIRY, TimeUnit.SECONDS);
        System.out.println("[JWT] Refresh token generation complete - User ID: " + user.getId() + ", Expiry time: " + new Date(expiryTime));
        
        return refreshToken;
    }
    
    /**
     * Generate token pair (Access Token and Refresh Token) for a user.
     * @param user User information
     * @return Generated token pair
     */
    public AuthTokens generateTokens(User user) {
        System.out.println("[JWT] Starting token pair generation - User: " + user.getEmail());
        String accessToken = generateAccessToken(user);
        String refreshToken = generateRefreshToken(user);
        
        System.out.println("[JWT] Token pair generation complete");
        return AuthTokens.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }
    
    /**
     * Validate Access Token.
     * @param token Token to validate
     * @return true if valid, false otherwise
     */
    public boolean validateAccessToken(String token) {
        System.out.println("[JWT] Starting access token validation");
        try {
            // Check if token is blacklisted
            if (isTokenBlacklisted(token)) {
                System.out.println("[JWT] Access token validation failed - Token is blacklisted");
                return false;
            }
            
            Jwts.parserBuilder()
                    .setSigningKey(getAccessTokenSecretKey())
                    .build()
                    .parseClaimsJws(token);
            System.out.println("[JWT] Access token validation successful");
            return true;
        } catch (Exception e) {
            System.out.println("[JWT] Access token validation failed - Reason: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Validate Refresh Token.
     * @param token Token to validate
     * @param userId User ID
     * @return true if valid, false otherwise
     */
    public boolean validateRefreshToken(String token, String userId) {
        System.out.println("[JWT] Starting refresh token validation - User ID: " + userId);
        try {
            // Check if token matches the one stored in Redis
            String key = REFRESH_TOKEN_PREFIX + userId;
            String storedToken = redisTemplate.opsForValue().get(key);
            if (storedToken == null || !storedToken.equals(token)) {
                System.out.println("[JWT] Refresh token validation failed - Token does not match stored token or not found");
                return false;
            }
            
            Jwts.parserBuilder()
                    .setSigningKey(getRefreshTokenSecretKey())
                    .build()
                    .parseClaimsJws(token);
            System.out.println("[JWT] Refresh token validation successful");
            return true;
        } catch (Exception e) {
            System.out.println("[JWT] Refresh token validation failed - Reason: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Extract user information from Access Token.
     * @param token JWT token
     * @return JWT payload
     */
    public JwtPayload extractAccessTokenPayload(String token) {
        System.out.println("[JWT] Starting access token payload extraction");
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(getAccessTokenSecretKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            
            JwtPayload payload = JwtPayload.builder()
                    .sub(claims.getSubject())
                    .email(claims.get("email", String.class))
                    .name(claims.get("name", String.class))
                    .role(claims.get("role", String.class))
                    .iat(claims.getIssuedAt().getTime())
                    .exp(claims.getExpiration().getTime())
                    .build();
            
            System.out.println("[JWT] Access token payload extraction complete - User: " + payload.getEmail());
            return payload;
        } catch (Exception e) {
            System.out.println("[JWT] Access token payload extraction failed - Reason: " + e.getMessage());
            throw new RuntimeException("Failed to extract payload from access token", e);
        }
    }
    
    /**
     * Extract user information from Refresh Token.
     * @param token Refresh Token
     * @return JWT payload
     */
    public JwtPayload extractRefreshTokenPayload(String token) {
        System.out.println("[JWT] Starting refresh token payload extraction");
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(getRefreshTokenSecretKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            
            JwtPayload payload = JwtPayload.builder()
                    .sub(claims.getSubject())
                    .iat(claims.getIssuedAt().getTime())
                    .exp(claims.getExpiration().getTime())
                    .build();
            
            System.out.println("[JWT] Refresh token payload extraction complete - User ID: " + payload.getSub());
            return payload;
        } catch (Exception e) {
            System.out.println("[JWT] Refresh token payload extraction failed - Reason: " + e.getMessage());
            throw new RuntimeException("Failed to extract payload from refresh token", e);
        }
    }
    
    /**
     * Add token to blacklist.
     * @param token Token to blacklist
     */
    public void blacklistToken(String token) {
        System.out.println("[JWT] Attempting to add token to blacklist");
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(getAccessTokenSecretKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            
            long expirationTime = claims.getExpiration().getTime();
            long now = System.currentTimeMillis();
            long ttl = (expirationTime - now) / 1000; // Convert to seconds
            
            if (ttl > 0) {
                // Add to Redis blacklist
                String blacklistKey = BLACKLIST_PREFIX + token;
                redisTemplate.opsForValue().set(blacklistKey, "blacklisted", ttl, TimeUnit.SECONDS);
                System.out.println("[JWT] Token successfully added to blacklist - Expiry in seconds: " + ttl);
            } else {
                System.out.println("[JWT] Skipping blacklist addition - Token already expired");
            }
        } catch (Exception e) {
            System.out.println("[JWT] Failed to add token to blacklist - Reason: " + e.getMessage());
        }
    }
    
    /**
     * Check if token is blacklisted.
     * @param token Token to check
     * @return true if blacklisted, false otherwise
     */
    public boolean isTokenBlacklisted(String token) {
        String blacklistKey = BLACKLIST_PREFIX + token;
        Boolean exists = redisTemplate.hasKey(blacklistKey);
        if (Boolean.TRUE.equals(exists)) {
            System.out.println("[JWT] Token is blacklisted");
            return true;
        }
        System.out.println("[JWT] Token is not blacklisted");
        return false;
    }
    
    /**
     * Delete a user's Refresh Token.
     * @param userId User ID
     */
    public void deleteRefreshToken(String userId) {
        String key = REFRESH_TOKEN_PREFIX + userId;
        Boolean deleted = redisTemplate.delete(key);
        if (Boolean.TRUE.equals(deleted)) {
            System.out.println("[JWT] Refresh token successfully deleted - User ID: " + userId);
        } else {
            System.out.println("[JWT] Failed to delete refresh token or token not found - User ID: " + userId);
        }
    }
    
    /**
     * Get secret key for Access Token signing.
     */
    private SecretKey getAccessTokenSecretKey() {
        return Keys.hmacShaKeyFor(JwtConstants.JWT_SECRET.getBytes(StandardCharsets.UTF_8));
    }
    
    /**
     * Get secret key for Refresh Token signing.
     */
    private SecretKey getRefreshTokenSecretKey() {
        return Keys.hmacShaKeyFor(JwtConstants.JWT_REFRESH_SECRET.getBytes(StandardCharsets.UTF_8));
    }
} 