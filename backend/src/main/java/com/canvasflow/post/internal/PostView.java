package com.canvasflow.post.internal;

/**
 * 조회용 게시글. text 는 기능 모듈(블러 등)의 render 파이프라인을 통과한 "가공된" 텍스트다.
 * (엔티티를 그대로 내보내지 않고, 조회 결과 전용 타입으로 반환)
 */
public record PostView(Long id, String text) {
}
