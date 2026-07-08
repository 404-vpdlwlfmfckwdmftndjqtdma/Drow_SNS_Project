package com.canvasflow.global.response;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * 모든 컨트롤러는 이 래퍼로 응답을 감싼다.
 *
 * 성공: return ResponseEntity.ok(ApiResponse.ok(data));
 * 실패: GlobalExceptionHandler 가 CanvasflowException 을 잡아 자동으로 변환한다.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(
        boolean success,
        String message,
        T data
) {
    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, null, data);
    }

    public static <T> ApiResponse<T> ok(String message, T data) {
        return new ApiResponse<>(true, message, data);
    }

    public static ApiResponse<Void> fail(String message) {
        return new ApiResponse<>(false, message, null);
    }
}
