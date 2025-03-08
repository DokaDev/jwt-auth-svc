package com.dokalab.auth.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Model class for authentication token information
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthTokens {
    private String accessToken;   // 접근 토큰
    private String refreshToken;  // 갱신 토큰
} 