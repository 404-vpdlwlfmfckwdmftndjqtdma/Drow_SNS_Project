package com.canvasflow.user.controller;

import com.canvasflow.user.dto.UpdateBioRequest;
import com.canvasflow.user.dto.UpdateNicknameRequest;
import com.canvasflow.user.dto.UpdateProfileImageRequest;
import com.canvasflow.user.dto.UserResponse;
import com.canvasflow.user.service.UserService;
import com.canvasflow.global.exception.CanvasflowException;
import com.canvasflow.global.exception.ErrorCode;
import com.canvasflow.global.response.ApiResponse;
import com.canvasflow.global.security.AuthMember;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * 담당: 회원(User) 기본 CRUD.
 * 로그인한 사용자 식별은 JwtAuthenticationFilter가 채워주는 @AuthenticationPrincipal AuthMember를 사용한다.
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> getMyInfo(@AuthenticationPrincipal AuthMember authMember) {
        requireLogin(authMember);
        return ResponseEntity.ok(ApiResponse.ok(userService.getMyInfo(authMember.userId())));
    }

    @PatchMapping("/me/nickname")
    public ResponseEntity<ApiResponse<UserResponse>> updateNickname(
            @AuthenticationPrincipal AuthMember authMember,
            @Valid @RequestBody UpdateNicknameRequest request) {
        requireLogin(authMember);
        return ResponseEntity.ok(ApiResponse.ok(userService.updateNickname(authMember.userId(), request)));
    }

    @PatchMapping("/me/profile-image")
    public ResponseEntity<ApiResponse<UserResponse>> updateProfileImage(
            @AuthenticationPrincipal AuthMember authMember,
            @Valid @RequestBody UpdateProfileImageRequest request) {
        requireLogin(authMember);
        return ResponseEntity.ok(ApiResponse.ok(userService.updateProfileImage(authMember.userId(), request)));
    }

    @PatchMapping("/me/bio")
    public ResponseEntity<ApiResponse<UserResponse>> updateBio(
            @AuthenticationPrincipal AuthMember authMember,
            @Valid @RequestBody UpdateBioRequest request) {
        requireLogin(authMember);
        return ResponseEntity.ok(ApiResponse.ok(userService.updateBio(authMember.userId(), request)));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<UserResponse>> getUser(@PathVariable Long userId) {
        return ResponseEntity.ok(ApiResponse.ok(userService.getMyInfo(userId)));
    }

    private void requireLogin(AuthMember authMember) {
        if (authMember == null) {
            throw new CanvasflowException(ErrorCode.UNAUTHORIZED);
        }
    }
}
