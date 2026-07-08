package com.canvasflow.domain.user.controller;

import com.canvasflow.domain.user.dto.UpdateNicknameRequest;
import com.canvasflow.domain.user.dto.UpdateProfileImageRequest;
import com.canvasflow.domain.user.dto.UserResponse;
import com.canvasflow.domain.user.service.UserService;
import com.canvasflow.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 담당: 회원(User) 기본 CRUD.
 * TODO: userId 는 JWT 필터 완성 전까지 @RequestHeader("X-User-Id") 로 임시 수신,
 *       완성 후 @AuthenticationPrincipal CustomUserDetails 로 교체.
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> getMyInfo(@RequestHeader("X-User-Id") Long userId) {
        return ResponseEntity.ok(ApiResponse.ok(userService.getMyInfo(userId)));
    }

    @PatchMapping("/me/nickname")
    public ResponseEntity<ApiResponse<UserResponse>> updateNickname(
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody UpdateNicknameRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(userService.updateNickname(userId, request)));
    }

    @PatchMapping("/me/profile-image")
    public ResponseEntity<ApiResponse<UserResponse>> updateProfileImage(
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody UpdateProfileImageRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(userService.updateProfileImage(userId, request)));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<UserResponse>> getUser(@PathVariable Long userId) {
        return ResponseEntity.ok(ApiResponse.ok(userService.getMyInfo(userId)));
    }
}
