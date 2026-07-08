package com.canvasflow.global.media;

import com.canvasflow.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 공통 미디어 업로드 API.
 * 프론트는 먼저 이 API로 파일을 업로드해 URL 을 받은 뒤,
 * 게시글/프로필 수정 요청 본문에 해당 URL 을 담아 보낸다.
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/media")
public class MediaController {

    private final MediaService mediaService;

    @PostMapping(value = "/upload", consumes = "multipart/form-data")
    public ResponseEntity<ApiResponse<MediaUploadResponse>> upload(@RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(ApiResponse.ok(mediaService.upload(file)));
    }

    @PostMapping(value = "/upload/batch", consumes = "multipart/form-data")
    public ResponseEntity<ApiResponse<List<MediaUploadResponse>>> uploadBatch(@RequestParam("files") List<MultipartFile> files) {
        return ResponseEntity.ok(ApiResponse.ok(mediaService.uploadBatch(files)));
    }
}
