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
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 담당: 회원가입 / 로그인 / 로그아웃 / 토큰 재발급.
 * TODO: refresh token 저장소(Redis 등) 연동, 로그아웃 시 토큰 무효화 로직 구현
 */
@RequiredArgsConstructor
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

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

    @Transactional(readOnly = true)
    public TokenResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new CanvasflowException(ErrorCode.INVALID_CREDENTIALS));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new CanvasflowException(ErrorCode.INVALID_CREDENTIALS);
        }

        String accessToken = jwtTokenProvider.createAccessToken(user.getId());
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getId());
        return new TokenResponse(accessToken, refreshToken);
    }

    public TokenResponse reissue(ReissueRequest request) {
        // TODO: refresh token 검증 후 새 access/refresh token 발급
        throw new UnsupportedOperationException("TODO: reissue 구현 필요");
    }

    public void logout(Long userId) {
        // TODO: refresh token 폐기 처리
    }
}
