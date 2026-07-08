package com.canvasflow.global.config;

import com.canvasflow.global.security.JwtAuthenticationFilter;
import com.canvasflow.global.security.handler.CustomAccessDeniedHandler;
import com.canvasflow.global.security.handler.CustomAuthenticationEntryPoint;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * 개발 모드(dev): 인증 없이 대부분의 API 호출 가능하도록 permitAll 처리되어 있음.
 * JwtAuthenticationFilter / CustomAuthenticationEntryPoint 등 인증 인프라는 완성되어 있으나,
 * 다른 도메인 컨트롤러가 X-User-Id -> @AuthenticationPrincipal 전환을 마치기 전까지는
 * anyRequest().authenticated() 로 바꾸지 않는다 (팀 전체 조율 후 전환 예정).
 */
@RequiredArgsConstructor
@EnableWebSecurity
@Configuration
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CustomAuthenticationEntryPoint authenticationEntryPoint;
    private final CustomAccessDeniedHandler accessDeniedHandler;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(e -> e
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler)
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(
                                "/api/v1/auth/**",
                                "/api/v1/posts/**",
                                "/api/v1/channels/**",
                                "/api/v1/search/**",
                                "/swagger-ui/**", "/v3/api-docs/**"
                        ).permitAll()
                        // TODO: 다른 도메인 컨트롤러가 X-User-Id -> @AuthenticationPrincipal 전환 완료 후 아래로 교체
                        // .anyRequest().authenticated()
                        .anyRequest().permitAll()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
