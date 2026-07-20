package com.canvasflow.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * 도메인별 에러 코드. 새 에러가 필요하면 여기에 추가하고
 * throw new CanvasflowException(ErrorCode.XXX) 형태로 사용한다.
 */
@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // Common
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "COMMON_001", "잘못된 요청입니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "COMMON_002", "인증이 필요합니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "COMMON_003", "접근 권한이 없습니다."),

    // Auth / User
    EMAIL_ALREADY_EXISTS(HttpStatus.CONFLICT, "AUTH_001", "이미 사용 중인 이메일입니다."),
    NICKNAME_ALREADY_EXISTS(HttpStatus.CONFLICT, "USER_002", "이미 사용 중인 닉네임입니다."),
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "AUTH_002", "이메일 또는 비밀번호가 올바르지 않습니다."),
    INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_003", "유효하지 않은 리프레시 토큰입니다."),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_004", "토큰이 만료되었습니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_001", "사용자를 찾을 수 없습니다."),

    // Post
    POST_NOT_FOUND(HttpStatus.NOT_FOUND, "POST_001", "게시글을 찾을 수 없습니다."),
    POST_MEDIA_LIMIT_EXCEEDED(HttpStatus.BAD_REQUEST, "POST_002", "첨부 가능한 파일 용량/개수를 초과했습니다."),
    POST_CONTENT_REQUIRED(HttpStatus.BAD_REQUEST, "POST_003", "게시글에 내용 또는 미디어가 하나 이상 필요합니다."),

    // Follow / Channel
    FOLLOW_NOT_FOUND(HttpStatus.NOT_FOUND, "FOLLOW_001", "팔로우 관계를 찾을 수 없습니다."),
    ALREADY_FOLLOWING(HttpStatus.CONFLICT, "FOLLOW_002", "이미 팔로우 중입니다."),
    CHANNEL_NOT_FOUND(HttpStatus.NOT_FOUND, "CHANNEL_001", "채널을 찾을 수 없습니다."),

    // Subscription
    SUBSCRIPTION_NOT_FOUND(HttpStatus.NOT_FOUND, "SUB_001", "구독 정보를 찾을 수 없습니다."),
    ALREADY_SUBSCRIBED(HttpStatus.CONFLICT, "SUB_002", "이미 구독 중입니다."),
    SUBSCRIPTION_REQUIRED(HttpStatus.FORBIDDEN, "SUB_003", "구독이 필요한 콘텐츠입니다."),
    TIER_NOT_FOUND(HttpStatus.NOT_FOUND, "SUB_004", "존재하지 않는 구독 등급입니다."),
    TIER_NOT_IN_CHANNEL(HttpStatus.BAD_REQUEST, "SUB_005", "해당 채널의 구독 등급이 아닙니다."),
    TIER_LEVEL_DUPLICATED(HttpStatus.CONFLICT, "SUB_006", "이미 존재하는 등급 레벨입니다."),
    TIER_LIMIT_EXCEEDED(HttpStatus.BAD_REQUEST, "SUB_007", "등급은 최대 5개까지 만들 수 있습니다."),

    // Purchase
    ALREADY_PURCHASED(HttpStatus.CONFLICT, "PUR_001", "이미 구매한 게시물입니다."),
    PURCHASE_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "PUR_002", "단건 구매가 불가능한 게시물입니다."),
    PURCHASE_SELF_POST(HttpStatus.BAD_REQUEST, "PUR_003", "본인의 게시물은 구매할 수 없습니다."),

    // Payment
    PAYMENT_ALREADY_PROCESSED(HttpStatus.CONFLICT, "PAY_001", "이미 처리된 결제입니다."),
    PAYMENT_CONFIRM_FAILED(HttpStatus.BAD_REQUEST, "PAY_002", "결제 승인에 실패했습니다."),
    PAYMENT_REQUIRED(HttpStatus.BAD_REQUEST, "PAY_003", "결제 정보가 필요합니다."),

    // Wallet
    WALLET_INSUFFICIENT_BALANCE(HttpStatus.BAD_REQUEST, "WALLET_001", "잔액이 부족합니다."),
    WALLET_INVALID_AMOUNT(HttpStatus.BAD_REQUEST, "WALLET_002", "금액은 0보다 커야 합니다."),

    // Order
    ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "ORDER_001", "주문을 찾을 수 없습니다."),

    // Comment / Like
    COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "COMMENT_001", "댓글을 찾을 수 없습니다."),
    ALREADY_LIKED(HttpStatus.CONFLICT, "LIKE_001", "이미 좋아요를 눌렀습니다."),
    LIKE_NOT_FOUND(HttpStatus.NOT_FOUND, "LIKE_002", "좋아요 정보를 찾을 수 없습니다."),

    // Media
    INVALID_FILE_EXTENSION(HttpStatus.BAD_REQUEST, "MEDIA_001", "허용되지 않은 파일 확장자입니다."),
    FILE_SIZE_EXCEEDED(HttpStatus.BAD_REQUEST, "MEDIA_002", "파일 용량 제한(50MB)을 초과했습니다."),
    MEDIA_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "MEDIA_003", "미디어 업로드에 실패했습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
