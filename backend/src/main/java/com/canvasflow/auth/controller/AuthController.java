package com.canvasflow.auth.controller;

import com.canvasflow.auth.dto.LoginRequest;
import com.canvasflow.auth.dto.PasswordResetDto;
import com.canvasflow.auth.dto.PasswordResetRequestDto;
import com.canvasflow.auth.dto.ReissueRequest;
import com.canvasflow.auth.dto.SignupRequest;
import com.canvasflow.auth.dto.TokenResponse;
import com.canvasflow.auth.service.AuthService;
import com.canvasflow.global.exception.CanvasflowException;
import com.canvasflow.global.exception.ErrorCode;
import com.canvasflow.global.response.ApiResponse;
import com.canvasflow.global.security.AuthMember;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<Void>> signup(@Valid @RequestBody SignupRequest request) {
        authService.signup(request);
        return ResponseEntity.ok(ApiResponse.ok("회원가입이 완료되었습니다.", null));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<TokenResponse>> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(authService.login(request)));
    }

    @PostMapping("/reissue")
    public ResponseEntity<ApiResponse<TokenResponse>> reissue(@Valid @RequestBody ReissueRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(authService.reissue(request)));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@AuthenticationPrincipal AuthMember authMember) {
        if (authMember == null) {
            throw new CanvasflowException(ErrorCode.UNAUTHORIZED);
        }
        authService.logout(authMember.userId());
        return ResponseEntity.ok(ApiResponse.ok("로그아웃되었습니다.", null));
    }

    // 가입 여부와 무관하게 항상 같은 메시지를 준다 - 이메일 존재 여부가 응답으로 노출되지 않도록.
    @PostMapping("/password/reset-request")
    public ResponseEntity<ApiResponse<Void>> requestPasswordReset(@Valid @RequestBody PasswordResetRequestDto request) {
        authService.requestPasswordReset(request.email());
        return ResponseEntity.ok(ApiResponse.ok("입력하신 이메일로 비밀번호 재설정 링크를 보내드렸습니다.", null));
    }

    @PostMapping("/password/reset")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@Valid @RequestBody PasswordResetDto request) {
        authService.resetPassword(request.token(), request.newPassword());
        return ResponseEntity.ok(ApiResponse.ok("비밀번호가 변경되었습니다.", null));
    }
}
