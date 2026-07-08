package com.canvasflow.domain.textblur;

import com.canvasflow.domain.post.extension.PostModule;
import com.canvasflow.domain.post.extension.PostViewContent;
import com.canvasflow.domain.subscription.service.ContentAccessService;
import com.canvasflow.domain.textblur.entity.TextBlurRange;
import com.canvasflow.domain.textblur.repository.TextBlurRangeRepository;
import com.canvasflow.global.exception.CanvasflowException;
import com.canvasflow.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * [텍스트 블러 모듈 - PostModule 구현]
 *
 * PostModule 을 구현하고 @Component 만 붙이면 Spring 컬렉션 주입으로 파이프라인에 자동으로 꽂힌다.
 * core(post)는 이 클래스의 존재를 모른다.
 *
 * 담당: 작성자가 본문 특정 구간을 블러 지정 → 비구독 열람자에게는 서버에서 ● 로 치환해 응답.
 * 원문은 구독자/작성자에게만 나간다. (프론트 CSS 블러는 장식일 뿐, 보안은 여기서 끝난다)
 *
 * 자기 테이블: text_blur_ranges (posts 에는 컬럼을 추가하지 않고 post_id 로만 참조)
 */
@RequiredArgsConstructor
@Component
public class TextBlurModule implements PostModule {

    public static final String KEY = "textBlur";
    private static final char MASK_CHAR = '●';

    private final TextBlurRangeRepository blurRangeRepository;
    // 다른 모듈(subscription)의 "공개 서비스"만 의존. 남의 리포지토리/엔티티 직접 접근 금지.
    private final ContentAccessService contentAccessService;

    @Override
    public String key() {
        return KEY;
    }

    // ── 저장 파이프라인: 전체 교체(replace) ───────────────────────────
    @Override
    public void saveExtension(Long postId, Long authorId, Object data) {
        blurRangeRepository.deleteByPostId(postId); // 기존 블러 제거 후 이번 것만 저장

        // data 는 Jackson 이 만든 Map{ "ranges" -> List<Map{ "start", "end" }> } 또는 null
        if (!(data instanceof Map<?, ?> map) || !(map.get("ranges") instanceof List<?> rangeList)) {
            return; // 구역이 없거나 형식이 안 맞으면 "블러 없음" 으로 확정
        }

        List<TextBlurRange> newRanges = new ArrayList<>();
        for (Object item : rangeList) {
            if (!(item instanceof Map<?, ?> range)) {
                throw new CanvasflowException(ErrorCode.INVALID_INPUT_VALUE);
            }
            int start = toInt(range.get("start"));
            int end = toInt(range.get("end"));
            if (start < 0 || end <= start) {
                throw new CanvasflowException(ErrorCode.INVALID_INPUT_VALUE);
            }
            newRanges.add(new TextBlurRange(postId, start, end));
        }
        blurRangeRepository.saveAll(newRanges);
    }

    // ── 조회 파이프라인: 비구독자면 마스킹 ─────────────────────────────
    @Override
    public void processView(PostViewContent content, Long viewerId) {
        List<TextBlurRange> ranges = blurRangeRepository.findByPostIdOrderByStartIdxAsc(content.getPostId());
        if (ranges.isEmpty()) {
            return; // 블러 없는 글은 그대로 통과
        }

        boolean unlocked = isUnlocked(content.getAuthorId(), viewerId);
        if (!unlocked) {
            content.setContent(mask(content.getContent(), ranges)); // ● 치환
        }

        // 프론트 렌더링용 메타데이터 (구간 위치 + 해제 여부)
        content.getExtensions().put(KEY, Map.of(
                "ranges", ranges.stream()
                        .map(r -> Map.of("start", r.getStartIdx(), "end", r.getEndIdx()))
                        .toList(),
                "unlocked", unlocked
        ));
    }

    private boolean isUnlocked(Long authorId, Long viewerId) {
        if (viewerId == null) return false;
        if (viewerId.equals(authorId)) return true; // 작성자 본인은 항상 원문
        return contentAccessService.isSubscribedToUser(viewerId, authorId);
    }

    /** [startIdx, endIdx) 구간을 ● 로 치환. 본문 길이를 벗어나는 값은 안전하게 잘라낸다. */
    private String mask(String text, List<TextBlurRange> ranges) {
        if (text == null || text.isEmpty()) return text;
        StringBuilder sb = new StringBuilder(text);
        for (TextBlurRange r : ranges) {
            int start = Math.max(0, r.getStartIdx());
            int end = Math.min(sb.length(), r.getEndIdx());
            for (int i = start; i < end; i++) {
                sb.setCharAt(i, MASK_CHAR);
            }
        }
        return sb.toString();
    }

    private int toInt(Object value) {
        if (value instanceof Number n) {
            return n.intValue();
        }
        throw new CanvasflowException(ErrorCode.INVALID_INPUT_VALUE);
    }
}
