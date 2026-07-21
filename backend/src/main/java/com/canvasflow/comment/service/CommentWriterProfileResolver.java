package com.canvasflow.comment.service;

import com.canvasflow.global.exception.CanvasflowException;
import com.canvasflow.global.exception.ErrorCode;
import com.canvasflow.user.UserFacade;
import com.canvasflow.user.UserProfileView;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * 댓글 작성자의 프로필(닉네임/프로필사진)을 UserFacade에서 조회하는 창구.
 * 탈퇴 등으로 유저가 이미 없어진 경우의 대체값 정책(닉네임 "user-{id}", 이미지 null)을 여기서 전담해서,
 * CommentService는 댓글 CRUD/브로드캐스트 로직에만 집중할 수 있게 한다.
 */
@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
class CommentWriterProfileResolver {

    private final UserFacade userFacade;

    /** 단건 조회. 유저가 없으면 예외 대신 대체 프로필을 반환한다. */
    UserProfileView resolve(Long userId) {
        try {
            return userFacade.getProfileView(userId);
        } catch (CanvasflowException e) {
            if (e.getErrorCode() == ErrorCode.USER_NOT_FOUND) {
                return fallback(userId);
            }
            throw e;
        }
    }

    /** 배치 조회 (N+1 방지). 요청한 id는 전부 결과 Map에 들어있는 게 보장된다 (없는 유저는 대체 프로필로 채워짐). */
    Map<Long, UserProfileView> resolveAll(Collection<Long> userIds) {
        Map<Long, UserProfileView> profiles = new HashMap<>(userFacade.getProfileViews(userIds));
        for (Long userId : userIds) {
            profiles.putIfAbsent(userId, fallback(userId));
        }
        return profiles;
    }

    private UserProfileView fallback(Long userId) {
        return new UserProfileView(userId, "user-" + userId, null, null);
    }
}
