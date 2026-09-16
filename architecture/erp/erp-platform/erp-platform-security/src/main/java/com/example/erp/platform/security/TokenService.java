package com.example.erp.platform.security;

import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class TokenService {

    public String issue(String appId) {
        return UUID.randomUUID().toString();
    }

    public String resolveAppId(String token) {
        return token == null || token.isBlank() ? null : "app";
    }
}