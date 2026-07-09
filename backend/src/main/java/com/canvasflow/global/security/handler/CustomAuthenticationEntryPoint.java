package com.canvasflow.global.security.handler;

import com.canvasflow.global.exception.ErrorCode;
import com.canvasflow.global.response.ApiResponse;
import com.canvasflow.global.security.JwtAuthenticationFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;

/**
 * 인증되지 않은 요청이 인증이 필요한 API에 접근했을 때 401 JSON 응답을 내려준다.
 * JwtAuthenticationFilter가 request attribute에 남긴 에러코드(만료/위조 등)를 우선 사용한다.
 * Spring Boot 4 / Jackson 3부터는 ObjectMapper(com.fasterxml.jackson) 대신
 * JsonMapper(tools.jackson.databind.json)가 자동 등록되는 Bean이다.
 */
@Component
@RequiredArgsConstructor
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final JsonMapper jsonMapper;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException)
            throws IOException {
        ErrorCode errorCode = (ErrorCode) request.getAttribute(JwtAuthenticationFilter.JWT_ERROR_CODE_ATTRIBUTE);
        if (errorCode == null) {
            errorCode = ErrorCode.UNAUTHORIZED;
        }

        response.setStatus(errorCode.getStatus().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(jsonMapper.writeValueAsString(ApiResponse.fail(errorCode.getMessage())));
    }
}
