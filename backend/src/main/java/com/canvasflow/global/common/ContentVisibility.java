package com.canvasflow.global.common;

/**
 * 게시글/채널 단위 공개 범위.
 * PUBLIC     - 전체공개
 * BLUR       - 미구독자에게 블러 처리 (미리보기 가능)
 * BLACKBOX   - 미구독자에게 완전 비공개(검은 박스) 처리
 * RESTRICTED - 미구독자 접근 자체 불가(목록에서도 제한적으로 노출)
 * PARTIAL    - 본문 일부만 공개, 나머지는 구독자 전용
 */
public enum ContentVisibility {
    PUBLIC,
    BLUR,
    BLACKBOX,
    RESTRICTED,
    PARTIAL
}
