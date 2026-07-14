package com.canvasflow.like;

/**
 * 댓글이 좋아요/좋아요취소 됐다는 사실만 담은 도메인 이벤트. like 모듈은 이걸 누가 구독해서
 * 알림을 만들고 실시간 브로드캐스트를 하는지 전혀 알지 못한다 (Domain Event + Pub/Sub).
 * comment 모듈이 이 이벤트를 구독해서 처리한다 - comment<->like 순환 의존을 없애기 위한 설계.
 * (게시글 좋아요는 post가 comment에 의존하지 않아 순환이 안 생기므로 이 이벤트를 안 쓰고 직접 호출한다.)
 */
public record TargetLikedEvent(Long commentId, Long likerId, String likerNickname, boolean liked, long likeCount) {
}
