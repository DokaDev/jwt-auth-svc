package com.dokalab.auth.controller;

import com.dokalab.auth.model.AuthResponse;
import com.dokalab.auth.model.AuthTokens;
import com.dokalab.auth.model.LoginCredentials;
import com.dokalab.auth.model.User;
import com.dokalab.auth.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    
    @Autowired
    public AuthController(AuthService authService) {
        this.authService = authService;
    }
    
    /**
     * Login API
     * 
     * @param credentials Login credentials
     * @return Authentication response
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginCredentials credentials) {
        System.out.println("[AUTH] Login request - Email: " + credentials.getEmail());
        try {
            AuthResponse response = authService.login(credentials);
            System.out.println("[AUTH] Login successful - User: " + response.getUser().getName());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.out.println("[AUTH] Login failed - Reason: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }
    
    /**
     * Token verification API
     * 
     * @param token JWT token
     * @return Verification result
     */
    @PostMapping("/verify")
    public ResponseEntity<Map<String, Boolean>> verifyToken(@RequestBody Map<String, String> tokenMap) {
        String token = tokenMap.get("token");
        if (token == null) {
            System.out.println("[AUTH] Token verification failed - Token not provided");
            return ResponseEntity.badRequest().build();
        }
        
        System.out.println("[AUTH] Token verification request");
        boolean isValid = authService.validateToken(token);
        Map<String, Boolean> response = new HashMap<>();
        response.put("valid", isValid);
        
        System.out.println("[AUTH] Token verification result: " + (isValid ? "valid" : "invalid"));
        return ResponseEntity.ok(response);
    }
    
    /**
     * Token refresh API
     * 
     * @param tokenMap Request body containing refresh token
     * @return New token pair
     */
    @PostMapping("/refresh")
    public ResponseEntity<AuthTokens> refreshToken(@RequestBody Map<String, String> tokenMap) {
        String refreshToken = tokenMap.get("refreshToken");
        if (refreshToken == null) {
            System.out.println("[AUTH] Token refresh failed - Refresh token not provided");
            return ResponseEntity.badRequest().build();
        }
        
        System.out.println("[AUTH] Token refresh request");
        try {
            AuthTokens tokens = authService.refreshToken(refreshToken);
            System.out.println("[AUTH] Token refresh successful");
            return ResponseEntity.ok(tokens);
        } catch (Exception e) {
            System.out.println("[AUTH] Token refresh failed - Reason: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }
    
    /**
     * Logout API
     * 
     * @param tokenMap Request body containing access token and user ID
     * @return Logout result
     */
    @PostMapping("/logout")
    public ResponseEntity<Map<String, Boolean>> logout(@RequestBody Map<String, String> tokenMap) {
        String accessToken = tokenMap.get("accessToken");
        String userId = tokenMap.get("userId");
        
        if (accessToken == null || userId == null) {
            System.out.println("[AUTH] Logout failed - Required parameters missing");
            return ResponseEntity.badRequest().build();
        }
        
        System.out.println("[AUTH] Logout request - User ID: " + userId);
        authService.logout(accessToken, userId);
        
        Map<String, Boolean> response = new HashMap<>();
        response.put("success", true);
        
        System.out.println("[AUTH] Logout successful");
        return ResponseEntity.ok(response);
    }
    
    /**
     * Extract user info from token API
     * 
     * @param tokenMap Request body containing token
     * @return User information
     */
    @PostMapping("/me")
    public ResponseEntity<User> getUserInfo(@RequestBody Map<String, String> tokenMap) {
        String token = tokenMap.get("token");
        if (token == null) {
            System.out.println("[AUTH] User info retrieval failed - Token not provided");
            return ResponseEntity.badRequest().build();
        }
        
        System.out.println("[AUTH] User info retrieval request");
        try {
            User user = authService.getUserFromToken(token);
            System.out.println("[AUTH] User info retrieval successful - User: " + user.getName());
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            System.out.println("[AUTH] User info retrieval failed - Reason: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }
} 