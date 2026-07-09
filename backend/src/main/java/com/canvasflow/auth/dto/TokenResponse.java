package com.canvasflow.auth.dto;

public record TokenResponse(
        String accessToken,
        String refreshToken
) {
}
