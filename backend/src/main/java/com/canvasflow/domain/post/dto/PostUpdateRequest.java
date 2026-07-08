package com.canvasflow.domain.post.dto;

import com.canvasflow.global.common.ContentVisibility;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PostUpdateRequest(
        @NotBlank @Size(max = 200) String title,
        String content,
        ContentVisibility visibility
) {
}
