package com.dokalab.auth.service;

import com.dokalab.auth.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final JwtService jwtService;
    
    @Autowired
    public AuthService(JwtService jwtService) {
        this.jwtService = jwtService;
    }
    
    /**
     * Process login
     * In a real application, this would authenticate the user,
     * and if successful, generate and return a JWT token.
     * 
     * @param credentials Login credentials
     * @return Authentication response
     */
    public AuthResponse login(LoginCredentials credentials) {
        System.out.println("[SERVICE] Login attempt - Email: " + credentials.getEmail());
        
        // Test user for demonstration
        if ("test@example.com".equals(credentials.getEmail()) && "password".equals(credentials.getPassword())) {
            System.out.println("[SERVICE] User authentication successful");
            
            User user = User.builder()
                    .id("1")
                    .email("test@example.com")
                    .name("Test User")
                    .role("user")
                    .build();
            
            // Generate JWT token
            System.out.println("[SERVICE] Generating JWT tokens");
            AuthTokens tokens = jwtService.generateTokens(user);
            System.out.println("[SERVICE] Token generation complete - Access token expiry time: " + JwtConstants.ACCESS_TOKEN_EXPIRY + " seconds");
            
            return AuthResponse.builder()
                    .user(user)
                    .tokens(tokens)
                    .build();
        }
        
        System.out.println("[SERVICE] Login failed - Invalid credentials");
        throw new RuntimeException("Invalid credentials");
    }
    
    /**
     * Get user information
     * Extracts user information from JWT token.
     * 
     * @param token JWT token
     * @return User information
     */
    public User getUserFromToken(String token) {
        System.out.println("[SERVICE] Attempting to extract user info from token");
        
        JwtPayload payload = jwtService.extractAccessTokenPayload(token);
        System.out.println("[SERVICE] User info extraction successful - User ID: " + payload.getSub());
        
        return User.builder()
                .id(payload.getSub())
                .email(payload.getEmail())
                .name(payload.getName())
                .role(payload.getRole())
                .build();
    }
    
    /**
     * Token refresh
     * Uses Refresh Token to issue a new Access Token.
     * 
     * @param refreshToken Refresh Token
     * @return New token pair
     */
    public AuthTokens refreshToken(String refreshToken) {
        System.out.println("[SERVICE] Token refresh attempt");
        
        JwtPayload payload = jwtService.extractRefreshTokenPayload(refreshToken);
        String userId = payload.getSub();
        System.out.println("[SERVICE] Refresh token payload extraction successful - User ID: " + userId);
        
        // Verify Refresh Token
        if (!jwtService.validateRefreshToken(refreshToken, userId)) {
            System.out.println("[SERVICE] Refresh token validation failed");
            throw new RuntimeException("Invalid refresh token");
        }
        
        System.out.println("[SERVICE] Refresh token validation successful");
        
        // Create temporary user info for testing
        User user = User.builder()
                .id(userId)
                .email("test@example.com")
                .name("Test User")
                .role("user")
                .build();
        
        // Generate new tokens
        System.out.println("[SERVICE] Generating new tokens");
        AuthTokens newTokens = jwtService.generateTokens(user);
        System.out.println("[SERVICE] New token generation complete");
        
        return newTokens;
    }
    
    /**
     * Process logout
     * Adds Access Token to blacklist and deletes Refresh Token.
     * 
     * @param accessToken Access Token
     * @param userId User ID
     */
    public void logout(String accessToken, String userId) {
        System.out.println("[SERVICE] Starting logout process - User ID: " + userId);
        
        // Add Access Token to blacklist
        System.out.println("[SERVICE] Adding access token to blacklist");
        jwtService.blacklistToken(accessToken);
        
        // Delete Refresh Token
        System.out.println("[SERVICE] Deleting refresh token");
        jwtService.deleteRefreshToken(userId);
        
        System.out.println("[SERVICE] Logout process complete");
    }
    
    /**
     * Token validation
     * 
     * @param token JWT token
     * @return true if valid, false otherwise
     */
    public boolean validateToken(String token) {
        System.out.println("[SERVICE] Attempting token validation");
        boolean isValid = jwtService.validateAccessToken(token);
        System.out.println("[SERVICE] Token validation result: " + (isValid ? "valid" : "invalid"));
        return isValid;
    }
} 