package com.canvasflow.global.media;

import com.canvasflow.global.exception.CanvasflowException;
import com.canvasflow.global.exception.ErrorCode;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Cloudinary 업로드 공통 서비스.
 * 게시글 첨부(이미지/영상), 프로필 이미지 업로드에서 공용으로 사용.
 */
@RequiredArgsConstructor
@Service
public class MediaService {

    private static final Set<String> ALLOWED_IMAGE_EXT = Set.of("jpg", "jpeg", "png", "gif", "webp");
    private static final Set<String> ALLOWED_VIDEO_EXT = Set.of("mp4", "mov", "webm");
    private static final long MAX_FILE_SIZE_BYTES = 50L * 1024 * 1024; // 50MB

    private final Cloudinary cloudinary;

    public MediaUploadResponse upload(MultipartFile file) {
        validate(file);
        MediaType mediaType = resolveMediaType(file);

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> result = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap("resource_type", mediaType == MediaType.VIDEO ? "video" : "image")
            );
            String url = (String) result.get("secure_url");
            return new MediaUploadResponse(url, mediaType, file.getSize());
        } catch (IOException e) {
            throw new CanvasflowException(ErrorCode.MEDIA_UPLOAD_FAILED);
        }
    }

    public List<MediaUploadResponse> uploadBatch(List<MultipartFile> files) {
        return files.stream().map(this::upload).toList();
    }

    private void validate(MultipartFile file) {
        if (file.getSize() > MAX_FILE_SIZE_BYTES) {
            throw new CanvasflowException(ErrorCode.FILE_SIZE_EXCEEDED);
        }
        String ext = extractExtension(file.getOriginalFilename());
        if (!ALLOWED_IMAGE_EXT.contains(ext) && !ALLOWED_VIDEO_EXT.contains(ext)) {
            throw new CanvasflowException(ErrorCode.INVALID_FILE_EXTENSION);
        }
    }

    private MediaType resolveMediaType(MultipartFile file) {
        String ext = extractExtension(file.getOriginalFilename());
        return ALLOWED_VIDEO_EXT.contains(ext) ? MediaType.VIDEO : MediaType.IMAGE;
    }

    private String extractExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            throw new CanvasflowException(ErrorCode.INVALID_FILE_EXTENSION);
        }
        return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
    }
}
