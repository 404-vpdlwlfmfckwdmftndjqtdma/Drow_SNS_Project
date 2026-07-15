package com.canvasflow.notification.controller;

import com.canvasflow.notification.dto.NotificationResponse;
import com.canvasflow.notification.service.NotificationService;
import com.canvasflow.global.exception.CanvasflowException;
import com.canvasflow.global.exception.ErrorCode;
import com.canvasflow.global.response.ApiResponse;
import com.canvasflow.global.security.AuthMember;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<NotificationResponse>>> getMyNotifications(
            @AuthenticationPrincipal AuthMember authMember,
            Pageable pageable) {
        requireLogin(authMember);
        return ResponseEntity.ok(ApiResponse.ok(
                notificationService.getMyNotifications(authMember.userId(), pageable).map(NotificationResponse::from)));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<ApiResponse<Long>> getUnreadCount(@AuthenticationPrincipal AuthMember authMember) {
        requireLogin(authMember);
        return ResponseEntity.ok(ApiResponse.ok(notificationService.getUnreadCount(authMember.userId())));
    }

    // 브라우저 EventSource는 커스텀 헤더(Authorization 포함)를 못 보내므로 subscribe는 쿼리 파라미터를 받는다.
    @GetMapping(value = "/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe(@RequestParam Long userId) {
        return notificationService.subscribe(userId);
    }

    @PatchMapping("/{notificationId}/read")
    public ResponseEntity<ApiResponse<Void>> markAsRead(
            @PathVariable Long notificationId,
            @AuthenticationPrincipal AuthMember authMember) {
        requireLogin(authMember);
        notificationService.markAsRead(notificationId, authMember.userId());
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    private void requireLogin(AuthMember authMember) {
        if (authMember == null) {
            throw new CanvasflowException(ErrorCode.UNAUTHORIZED);
        }
    }
}
