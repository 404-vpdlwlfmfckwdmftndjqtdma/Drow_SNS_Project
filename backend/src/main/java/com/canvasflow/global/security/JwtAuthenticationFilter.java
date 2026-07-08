package com.canvasflow.global.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Authorization: Bearer {token} 헤더를 읽어 SecurityContext 에 인증 정보를 채워넣는다.
 * TODO: 토큰 파싱 -> CustomUserDetails 조회 -> SecurityContextHolder 설정
 * 현재는 dev 모드이므로 미구현 상태로 통과(no-op)시킨다.
 */
@RequiredArgsConstructor
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        // TODO: 헤더에서 토큰 추출 후 검증, 인증 객체 SecurityContext 에 설정
        filterChain.doFilter(request, response);
    }
}
