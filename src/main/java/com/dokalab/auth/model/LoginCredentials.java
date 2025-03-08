package com.dokalab.auth.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Model class for login request information
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginCredentials {
    private String email;    // Email
    private String password; // Password
} 