package com.dokalab.auth.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 사용자 정보를 담는 모델 클래스
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
    private String id;      // 사용자 ID
    private String email;   // 이메일
    private String name;    // 이름
    private String role;    // 역할
} 