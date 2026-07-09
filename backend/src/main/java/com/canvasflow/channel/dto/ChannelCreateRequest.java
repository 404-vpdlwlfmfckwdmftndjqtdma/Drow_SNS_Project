package com.canvasflow.channel.dto;

import com.canvasflow.global.common.ContentVisibility;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChannelCreateRequest(
        @NotBlank @Size(max = 50) String name,
        @Size(max = 500) String description,
        ContentVisibility defaultVisibility
) {
}
