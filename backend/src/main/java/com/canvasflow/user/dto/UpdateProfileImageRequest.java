package com.canvasflow.user.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateProfileImageRequest(
        @NotBlank String profileImageUrl
) {
}
