package com.canvasflow.notification;

// 알림이 가리키는 대상의 종류. like 모듈의 LikeTargetType과 값은 같지만,
// 도메인 간 직접 참조를 피하기 위해 notification 모듈이 자체적으로 갖고 있는 타입이다.
public enum NotificationTargetType {
    POST,
    COMMENT
}
