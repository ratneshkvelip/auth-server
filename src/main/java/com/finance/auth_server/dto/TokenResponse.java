package com.finance.auth_server.dto;

public record TokenResponse(
        String tokenType,
        String accessToken,
        String refreshToken,
        long expiresIn
) { }
