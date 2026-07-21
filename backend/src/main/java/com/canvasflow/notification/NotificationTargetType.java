package com.canvasflow.notification;

// 알림이 가리키는 대상의 종류. like 모듈의 LikeTargetType과 값은 같지만,
// 도메인 간 직접 참조를 피하기 위해 notification 모듈이 자체적으로 갖고 있는 타입이다.
// COMMENT는 쓰지 않는다 - 댓글은 단독 페이지가 없어서, 댓글 관련 알림(좋아요/답글)도 결국
// 그 댓글이 달린 게시글을 targetId로 저장한다(POST). USER는 팔로우 알림 전용 - 클릭 시 그 사람의 프로필로 이동.
public enum NotificationTargetType {
    POST,
    COMMENT,
    USER
}
