package com.canvasflow.domain.auth.service;

import com.canvasflow.domain.auth.dto.LoginRequest;
import com.canvasflow.domain.auth.dto.ReissueRequest;
import com.canvasflow.domain.auth.dto.SignupRequest;
import com.canvasflow.domain.auth.dto.TokenResponse;
import com.canvasflow.domain.user.entity.User;
import com.canvasflow.domain.user.repository.UserRepository;
import com.canvasflow.global.exception.CanvasflowException;
import com.canvasflow.global.exception.ErrorCode;
import com.canvasflow.global.security.JwtTokenProvider;
import com.canvasflow.global.security.RefreshToken;
import com.canvasflow.global.security.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 담당: 회원가입 / 로그인 / 로그아웃 / 토큰 재발급.
 * refresh token은 PostgreSQL(refresh_tokens 테이블)에 userId 기준으로 저장/대조한다.
 */
@RequiredArgsConstructor
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;

    @Transactional
    public void signup(SignupRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new CanvasflowException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }
        User user = User.builder()
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .nickname(request.nickname())
                .build();
        userRepository.save(user);
    }

    @Transactional
    public TokenResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new CanvasflowException(ErrorCode.INVALID_CREDENTIALS));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new CanvasflowException(ErrorCode.INVALID_CREDENTIALS);
        }

        String accessToken = jwtTokenProvider.createAccessToken(user.getId());
        String refreshToken = issueRefreshToken(user.getId());
        return new TokenResponse(accessToken, refreshToken);
    }

    @Transactional
    public TokenResponse reissue(ReissueRequest request) {
        String requestRefreshToken = request.refreshToken();

        if (!jwtTokenProvider.validateToken(requestRefreshToken)) {
            throw new CanvasflowException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        Long userId = jwtTokenProvider.getUserId(requestRefreshToken);
        RefreshToken savedRefreshToken = refreshTokenRepository.findById(userId)
                .orElseThrow(() -> new CanvasflowException(ErrorCode.INVALID_REFRESH_TOKEN));

        if (!savedRefreshToken.matches(requestRefreshToken)) {
            throw new CanvasflowException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        String newAccessToken = jwtTokenProvider.createAccessToken(userId);
        String newRefreshToken = issueRefreshToken(userId);
        return new TokenResponse(newAccessToken, newRefreshToken);
    }

    @Transactional
    public void logout(Long userId) {
        refreshTokenRepository.deleteById(userId);
    }

    private String issueRefreshToken(Long userId) {
        String refreshToken = jwtTokenProvider.createRefreshToken(userId);
        LocalDateTime expiresAt = LocalDateTime.now()
                .plusSeconds(jwtTokenProvider.getRefreshTokenExpirationMs() / 1000);

        refreshTokenRepository.findById(userId)
                .ifPresentOrElse(
                        existing -> existing.update(refreshToken, expiresAt),
                        () -> refreshTokenRepository.save(new RefreshToken(userId, refreshToken, expiresAt))
                );

        return refreshToken;
    }
}
