package com.canvasflow.domain.like.service;

import com.canvasflow.domain.like.dto.LikeResponse;
import com.canvasflow.domain.like.entity.Like;
import com.canvasflow.domain.like.entity.LikeTargetType;
import com.canvasflow.domain.like.repository.LikeRepository;
import com.canvasflow.domain.user.entity.User;
import com.canvasflow.domain.user.repository.UserRepository;
import com.canvasflow.global.exception.CanvasflowException;
import com.canvasflow.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class LikeService {

    private final LikeRepository likeRepository;
    private final UserRepository userRepository;
    // TODO: NotificationService 주입 -> 좋아요 발생 시 대상 작성자에게 알림

    @Transactional
    public LikeResponse like(Long userId, LikeTargetType targetType, Long targetId) {
        if (likeRepository.existsByUserIdAndTargetTypeAndTargetId(userId, targetType, targetId)) {
            throw new CanvasflowException(ErrorCode.ALREADY_LIKED);
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CanvasflowException(ErrorCode.USER_NOT_FOUND));
        likeRepository.save(Like.builder().user(user).targetType(targetType).targetId(targetId).build());
        return new LikeResponse(true, likeRepository.countByTargetTypeAndTargetId(targetType, targetId));
    }

    @Transactional
    public LikeResponse unlike(Long userId, LikeTargetType targetType, Long targetId) {
        Like like = likeRepository.findByUserIdAndTargetTypeAndTargetId(userId, targetType, targetId)
                .orElseThrow(() -> new CanvasflowException(ErrorCode.LIKE_NOT_FOUND));
        likeRepository.delete(like);
        return new LikeResponse(false, likeRepository.countByTargetTypeAndTargetId(targetType, targetId));
    }
}
