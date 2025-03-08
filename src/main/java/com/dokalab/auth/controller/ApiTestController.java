package com.dokalab.auth.controller;

import com.dokalab.auth.model.JwtPayload;
import com.dokalab.auth.service.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ApiTestController {

    private final JwtService jwtService;
    
    @Autowired
    public ApiTestController(JwtService jwtService) {
        this.jwtService = jwtService;
    }
    
    /**
     * Public API test endpoint
     * No authentication required
     * 
     * @return Test data
     */
    @GetMapping("/public")
    public ResponseEntity<Map<String, String>> publicEndpoint() {
        System.out.println("[API] Public API called");
        Map<String, String> response = new HashMap<>();
        response.put("message", "This is public data, no auth required");
        return ResponseEntity.ok(response);
    }
    
    /**
     * Protected API test endpoint
     * Authentication required
     * 
     * @param authorization Authorization header
     * @return Test data
     */
    @GetMapping("/protected")
    public ResponseEntity<Map<String, Object>> protectedEndpoint(@RequestHeader(value = "Authorization", required = false) String authorization) {
        System.out.println("[API] Protected API called");
        
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            System.out.println("[API] Protected API access failed - No authentication token");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        String token = authorization.substring(7);
        if (!jwtService.validateAccessToken(token)) {
            System.out.println("[API] Protected API access failed - Invalid token");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "This is protected data, authenticated user access only");
        
        try {
            JwtPayload payload = jwtService.extractAccessTokenPayload(token);
            response.put("user", payload);
            System.out.println("[API] Protected API access successful - User: " + payload.getName());
        } catch (Exception e) {
            System.out.println("[API] Payload extraction failed - Reason: " + e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Admin-only API test endpoint
     * Authentication and admin role required
     * 
     * @param authorization Authorization header
     * @return Test data
     */
    @GetMapping("/admin")
    public ResponseEntity<Map<String, Object>> adminEndpoint(@RequestHeader(value = "Authorization", required = false) String authorization) {
        System.out.println("[API] Admin API called");
        
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            System.out.println("[API] Admin API access failed - No authentication token");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        String token = authorization.substring(7);
        if (!jwtService.validateAccessToken(token)) {
            System.out.println("[API] Admin API access failed - Invalid token");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        JwtPayload payload = jwtService.extractAccessTokenPayload(token);
        if (!"admin".equals(payload.getRole())) {
            System.out.println("[API] Admin API access failed - Insufficient permissions (Role: " + payload.getRole() + ")");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "This is admin data, admin role required");
        response.put("user", payload);
        
        System.out.println("[API] Admin API access successful - Admin: " + payload.getName());
        return ResponseEntity.ok(response);
    }
} 