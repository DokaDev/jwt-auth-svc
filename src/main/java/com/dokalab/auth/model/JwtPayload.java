package com.dokalab.auth.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Model class for JWT token payload information
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JwtPayload {
    private String sub;     // User ID (subject)
    private String email;   // Email
    private String name;    // Name
    private String role;    // Role
    private long iat;       // Issued at time
    private long exp;       // Expiration time
} 