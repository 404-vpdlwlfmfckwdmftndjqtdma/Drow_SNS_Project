package com.canvasflow.global.exception;

import com.canvasflow.global.response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CanvasflowException.class)
    public ResponseEntity<ApiResponse<Void>> handleCanvasflowException(CanvasflowException e, HttpServletRequest request) {
        ErrorCode errorCode = e.getErrorCode();
        // 예상된 실패(404/403 등)는 스택트레이스 없이 한 줄만 - 로그 노이즈 방지
        log.warn("[{}] {} {} - {}", errorCode.getCode(), request.getMethod(), request.getRequestURI(), errorCode.getMessage());
        return ResponseEntity.status(errorCode.getStatus())
                .body(ApiResponse.fail(errorCode.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(MethodArgumentNotValidException e, HttpServletRequest request) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .orElse(ErrorCode.INVALID_INPUT_VALUE.getMessage());
        log.warn("[VALIDATION] {} {} - {}", request.getMethod(), request.getRequestURI(), message);
        return ResponseEntity.status(ErrorCode.INVALID_INPUT_VALUE.getStatus())
                .body(ApiResponse.fail(message));
    }

    /**
     * 매핑되지 않은 경로 요청. 그냥 두면 아래 Exception 핸들러에 잡혀 500으로 나가는데,
     * 이건 서버 잘못이 아니라 잘못된 요청이므로 404로 돌려준다.
     * (오타 난 URL을 서버 장애로 오인해 원인을 엉뚱한 데서 찾게 되는 것을 막는다.)
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNoResourceFound(NoResourceFoundException e, HttpServletRequest request) {
        log.warn("[404] {} {} - 매핑된 핸들러 없음", request.getMethod(), request.getRequestURI());
        return ResponseEntity.status(ErrorCode.ENDPOINT_NOT_FOUND.getStatus())
                .body(ApiResponse.fail(ErrorCode.ENDPOINT_NOT_FOUND.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception e, HttpServletRequest request) {
        // 예상 못 한 500은 반드시 스택트레이스까지 남긴다 (이게 없으면 원인 추적 자체가 불가능).
        // 응답 본문에는 내부 정보를 노출하지 않고, 상세는 서버 로그에만 남긴다.
        log.error("[UNHANDLED] {} {} - {}", request.getMethod(), request.getRequestURI(), e.getMessage(), e);
        return ResponseEntity.internalServerError()
                .body(ApiResponse.fail("서버 내부 오류가 발생했습니다."));
    }
}
