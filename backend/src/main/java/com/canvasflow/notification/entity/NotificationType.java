package com.canvasflow.notification.entity;

public enum NotificationType {
    COMMENT,        // 내 게시글에 댓글
    LIKE,           // 내 게시글/댓글에 좋아요
    NEW_FOLLOWER,   // 나를 팔로우
    NEW_POST,       // 팔로우한 사용자가 새 게시글 작성
    NEW_SUBSCRIBER  // 내 채널/게시글에 새 구독자
}
