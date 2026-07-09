package com.canvasflow.channel.controller;

import com.canvasflow.channel.dto.ChannelCreateRequest;
import com.canvasflow.channel.dto.ChannelResponse;
import com.canvasflow.channel.service.ChannelService;
import com.canvasflow.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/channels")
public class ChannelController {

    private final ChannelService channelService;

    @PostMapping
    public ResponseEntity<ApiResponse<Long>> create(
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody ChannelCreateRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(channelService.create(userId, request)));
    }

    @GetMapping("/{channelId}")
    public ResponseEntity<ApiResponse<ChannelResponse>> getDetail(@PathVariable Long channelId) {
        return ResponseEntity.ok(ApiResponse.ok(channelService.getDetail(channelId)));
    }

    @PostMapping("/{channelId}/members")
    public ResponseEntity<ApiResponse<Void>> addMember(
            @PathVariable Long channelId,
            @RequestHeader("X-User-Id") Long userId) {
        channelService.addMember(channelId, userId);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @DeleteMapping("/{channelId}/members")
    public ResponseEntity<ApiResponse<Void>> removeMember(
            @PathVariable Long channelId,
            @RequestHeader("X-User-Id") Long userId) {
        channelService.removeMember(channelId, userId);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}
