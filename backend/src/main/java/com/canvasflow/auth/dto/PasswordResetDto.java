package com.canvasflow.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PasswordResetDto(
        @NotBlank String token,
        @NotBlank @Size(min = 8, max = 64) String newPassword
) {
}
