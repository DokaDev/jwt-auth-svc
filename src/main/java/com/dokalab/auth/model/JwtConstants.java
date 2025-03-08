package com.dokalab.auth.model;

/**
 * Class for defining JWT-related constants
 */
public class JwtConstants {
    // JWT secret keys (should be managed with environment variables in production)
    public static final String JWT_SECRET = "a1b2c3d4e5f6g7h8i9j0k1l2m3n4o5p6q7r8s9t0u1v2w3x4y5z6";
    public static final String JWT_REFRESH_SECRET = "z6y5x4w3v2u1t0s9r8q7p6o5n4m3l2k1j0i9h8g7f6e5d4c3b2a1";
    
    // Token expiration times (in seconds)
    public static final int ACCESS_TOKEN_EXPIRY = 15;  // 15 seconds (short time for testing)
    public static final int REFRESH_TOKEN_EXPIRY = 600; // 10 minutes
    
    // Token type
    public static final String TOKEN_TYPE = "Bearer";
} 