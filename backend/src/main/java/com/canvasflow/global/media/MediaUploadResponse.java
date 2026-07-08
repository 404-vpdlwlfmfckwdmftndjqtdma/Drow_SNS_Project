package com.canvasflow.global.media;

public record MediaUploadResponse(
        String url,
        MediaType mediaType,
        long sizeBytes
) {
}
