package com.canvasflow.textblur;

import com.canvasflow.post.PostExtension;
import com.canvasflow.textblur.internal.TextBlurRange;
import com.canvasflow.textblur.internal.TextBlurRangeRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * [텍스트 블러 모듈] 자기 테이블(text_blur_ranges)을 소유하는 독립 모듈.
 * post 의 창구 PostExtension 만 의존한다 — post 내부(엔티티/리포지토리)는 못 건드린다.
 */
@Component
public class TextBlurExtension implements PostExtension {

    private final TextBlurRangeRepository rangeRepository;

    public TextBlurExtension(TextBlurRangeRepository rangeRepository) {
        this.rangeRepository = rangeRepository;
    }

    @Override
    public String key() {
        return "textBlur";
    }

    @Override
    public void apply(Long postId, Object section) {
        // 전체 교체(replace): 기존 블러 지우고 이번에 온 것만 저장
        rangeRepository.deleteByPostId(postId);

        // section = {"ranges": [ {"start":9,"end":15}, ... ]} (또는 null)
        if (!(section instanceof Map<?, ?> map) || !(map.get("ranges") instanceof List<?> ranges)) {
            return;
        }

        List<TextBlurRange> rows = new ArrayList<>();
        for (Object item : ranges) {
            if (item instanceof Map<?, ?> r
                    && r.get("start") instanceof Number start
                    && r.get("end") instanceof Number end) {
                rows.add(new TextBlurRange(postId, start.intValue(), end.intValue()));
            }
        }
        rangeRepository.saveAll(rows);
    }

    @Override
    public String render(Long postId, String text, boolean unlocked) {
        if (unlocked) {
            return text;   // 구매/구독으로 잠금 해제된 뷰어(작성자 포함)에게는 원문 그대로
        }
        if (text == null || text.isEmpty()) {
            return text;
        }
        List<TextBlurRange> ranges = rangeRepository.findByPostId(postId);
        if (ranges.isEmpty()) {
            return text; // 블러 없는 글은 그대로
        }
        // 각 [start, end) 구간을 ● 로 치환. 본문 길이를 벗어나는 값은 안전하게 자른다.
        StringBuilder sb = new StringBuilder(text);
        for (TextBlurRange r : ranges) {
            int start = Math.max(0, r.getStartIdx());
            int end = Math.min(sb.length(), r.getEndIdx());
            for (int i = start; i < end; i++) {
                sb.setCharAt(i, '●');
            }
        }
        return sb.toString();
    }
}
