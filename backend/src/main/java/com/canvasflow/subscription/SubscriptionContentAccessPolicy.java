package com.canvasflow.subscription;

import com.canvasflow.post.ContentAccessPolicy;
import com.canvasflow.post.PostReader;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@RequiredArgsConstructor
public class SubscriptionContentAccessPolicy implements ContentAccessPolicy {

    /** 지금은 모듈별 개별 잠금이 아니라 글 단위 잠금이므로, 해제 시 전체 키를 반환 */
    private static final Set<String> ALL_KEYS = Set.of("textBlur", "imageBlur", "videoWatermark");

    private final ContentAccessService contentAccessService;
    private final PostReader postReader;         // requiredlevel을 알아야 판정 가능

    @Override
    public Set<String> unlockedKeys(Long viewerId, Long postId, Long authorId) {
        int requiredLevel = postReader.getRequiredlevel(postId);  // TODO postreader에 메서드 추가하기
        Long channelId = authorId;

        boolean canView = contentAccessService.canView(
                viewerId, channelId, postId, authorId, requiredLevel);

        return  canView ? ALL_KEYS : Set.of();
    }

}
