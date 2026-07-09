package com.canvasflow.user.service;

import com.canvasflow.user.dto.MyPageResponse;
import com.canvasflow.user.dto.UserResponse;
import com.canvasflow.user.repository.UserRepository;
import com.canvasflow.global.exception.CanvasflowException;
import com.canvasflow.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 마이페이지 요약 정보 집계.
 * TODO: PostRepository / FollowRepository / SubscriptionRepository / NotificationRepository 주입 후
 *       각 count 쿼리 연결 (현재는 0으로 placeholder)
 */
@RequiredArgsConstructor
@Service
public class MyPageService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public MyPageResponse getSummary(Long userId) {
        UserResponse profile = UserResponse.from(userRepository.findById(userId)
                .orElseThrow(() -> new CanvasflowException(ErrorCode.USER_NOT_FOUND)));

        // TODO: 실제 집계 쿼리로 교체
        return new MyPageResponse(profile, 0L, 0L, 0L, 0L, 0L);
    }
}
