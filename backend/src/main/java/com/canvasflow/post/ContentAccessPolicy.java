package com.canvasflow.post;

import java.util.Set;

/**
 * [post 모듈의 창구] 이 뷰어가 이 글에 대해 잠금 해제한 기능 key 목록을 물어보는 인터페이스.
 * 개별구매/채널구독 정보를 취합하는 entitlement 도메인이 구현한다.
 * post(및 textblur/imageblur 등 확장 모듈)는 구매/구독 로직을 몰라도 되고, 결과 Set만 보고 판단한다.
 */
public interface ContentAccessPolicy {

    /**
     * 이 뷰어가 잠금 해제한 기능들의 key 목록.
     * 들어있으면 원본, 없으면 블러.
     * 예) { "textBlur" }  → 텍스트는 원본, 이미지는 블러
     */
    Set<String> unlockedKeys(Long viewerId, Long postId, Long authorId);
}
