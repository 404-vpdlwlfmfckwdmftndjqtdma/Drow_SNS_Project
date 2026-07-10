package com.canvasflow.global.common;

/**
 * 게시글 단위 공개 범위.
 * PUBLIC  - 전체공개
 * PRIVATE - 나만보기 (마이페이지에서만 노출)
 * LOCKED  - 구독 필요 (텍스트/이미지 블러, 동영상 재생불가는 콘텐츠 종류별로 적용)
 */
public enum ContentVisibility {
    PUBLIC,
    PRIVATE,
    LOCKED
}
