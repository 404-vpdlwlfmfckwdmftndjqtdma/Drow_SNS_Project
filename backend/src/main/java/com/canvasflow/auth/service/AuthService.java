package com.canvasflow.auth.service;

import com.canvasflow.auth.AuthFacade;
import com.canvasflow.auth.dto.LoginRequest;
import com.canvasflow.auth.dto.ReissueRequest;
import com.canvasflow.auth.dto.SignupRequest;
import com.canvasflow.auth.dto.TokenResponse;
import com.canvasflow.auth.entity.PasswordResetToken;
import com.canvasflow.auth.repository.PasswordResetTokenRepository;
import com.canvasflow.user.UserFacade;
import com.canvasflow.global.exception.CanvasflowException;
import com.canvasflow.global.exception.ErrorCode;
import com.canvasflow.global.mail.MailService;
import com.canvasflow.global.security.JwtTokenProvider;
import com.canvasflow.global.security.RefreshToken;
import com.canvasflow.global.security.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 담당: 회원가입 / 로그인 / 로그아웃 / 토큰 재발급.
 * User Entity/Repository는 직접 접근하지 않고, user 모듈 기본 패키지의 UserFacade 인터페이스를
 * 통해서만 접근한다 (모듈 경계 준수 + 구현체(UserService)가 아닌 인터페이스로 의존).
 * 이 클래스 자신도 auth 모듈 기본 패키지의 AuthFacade를 구현해서, 다른 모듈이 나중에 auth 기능이
 * 필요해지면 AuthService를 직접 참조하지 않고 이 인터페이스로만 의존할 수 있게 해둔다.
 * refresh token은 PostgreSQL(refresh_tokens 테이블)에 userId 기준으로 저장/대조한다.
 */
@RequiredArgsConstructor
@Service
public class AuthService implements AuthFacade {

    // 비밀번호 재설정 링크의 유효 시간. 메일 발송/토큰 검증 양쪽에서 이 값을 기준으로 계산한다.
    private static final int RESET_TOKEN_EXPIRATION_MINUTES = 30;

    private final UserFacade userFacade;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final MailService mailService;

    @Override
    @Transactional
    public void signup(SignupRequest request) {
        if (userFacade.existsByEmail(request.email())) {
            throw new CanvasflowException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }
        userFacade.createUser(request.email(), request.password(), request.nickname());
    }

    @Override
    @Transactional
    public TokenResponse login(LoginRequest request) {
        Long userId = userFacade.verifyCredentials(request.email(), request.password());

        String accessToken = jwtTokenProvider.createAccessToken(userId);
        String refreshToken = issueRefreshToken(userId);
        return new TokenResponse(accessToken, refreshToken);
    }

    @Override
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

    @Override
    @Transactional
    public void logout(Long userId) {
        refreshTokenRepository.deleteById(userId);
    }

    @Override
    @Transactional
    public void requestPasswordReset(String email) {
        Long userId = userFacade.findIdByEmail(email);
        if (userId == null) {
            // 가입 안 된 이메일이어도 여기서 조용히 종료한다 - 컨트롤러는 항상 같은 성공 메시지를 주므로
            // 이 이메일이 가입되어 있는지 여부가 응답으로 노출되지 않는다.
            return;
        }

        String token = UUID.randomUUID().toString();
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(RESET_TOKEN_EXPIRATION_MINUTES);
        passwordResetTokenRepository.save(new PasswordResetToken(userId, token, expiresAt));
        mailService.sendPasswordResetEmail(email, token);
    }

    @Override
    @Transactional
    public void resetPassword(String token, String newPassword) {
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(token)
                .orElseThrow(() -> new CanvasflowException(ErrorCode.INVALID_RESET_TOKEN));

        if (resetToken.isUsed() || resetToken.isExpired()) {
            throw new CanvasflowException(ErrorCode.INVALID_RESET_TOKEN);
        }

        userFacade.updatePassword(resetToken.getUserId(), newPassword);
        resetToken.markUsed();
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
