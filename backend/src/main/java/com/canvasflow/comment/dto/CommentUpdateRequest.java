package com.canvasflow.comment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// 본문만 수정 가능. writer 본인만 호출 가능하며 검증은 CommentService에서 수행.
public record CommentUpdateRequest(
        @NotBlank @Size(max = 1000) String content
) {
}
