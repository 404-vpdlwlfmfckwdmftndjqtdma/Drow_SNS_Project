package com.canvasflow.textblur;

import com.canvasflow.post.PostExtension;
import com.canvasflow.textblur.internal.TextBlurRange;
import com.canvasflow.textblur.internal.TextBlurRangeRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

/**
 * 텍스트 부분 블러 기능.
 *
 * 저장(apply): 작성 요청의 extensions.textBlur 칸에서 구간을 받아 저장
 *  {"ranges": [{"start": 9, "end": 15}, {"start": 30, "end": 42}]}
 *
 *  조회(render): 저장된 구간을 ● 로 치환한 본문을 반환
 *    "안녕하세요 여기부터비밀" -> "안녕하세요 ●●●●●●"
 *
 *  주의 : render는 "이 뷰어에게 블러를 보여줘야 하는가"를 판단하지 않는다.
 *        그 판단(구독 등급 확인)은 post 조회 서비스가 ContentAccessService로 하고,
 *        블러가 필요한 뷰어에게만 이 render 결과를 내려줘야 한다.
 *
 */
@Component
@RequiredArgsConstructor
public class TextBlurExtension implements PostExtension {

    private static final char BLUR_CHAR = '●';

    private final TextBlurRangeRepository textBlurRangeRepository;
    private final ObjectMapper objectMapper;

    @Override
    public String key() {
        return "textBlur";
    }

    @Override
    @Transactional
    public void apply(Long postId, Object section) {
        // 글 수정 시 기존 구간은 항상 초기화 (section이 null이면 "블러 없음"으로 수정한 것)
        textBlurRangeRepository.deleteByPostId(postId);
        if (section == null) {
            return;
        }

        RangesPayload payload = objectMapper.convertValue(section, RangesPayload.class);
        if (payload.ranges() == null || payload.ranges().isEmpty()) {
            return;
        }

        List<TextBlurRange> entities = payload.ranges().stream()
                .map(r -> TextBlurRange.builder()
                        .postId(postId)
                        .startIdx(r.start())
                        .endIdx(r.end())
                        .build())              // 구간 유효성(start<end 등)은 엔티티 생성자가 검증
                .toList();

        textBlurRangeRepository.saveAll(entities);
    }

    @Override
    @Transactional(readOnly = true)
    public String render(Long postId, String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }

        List<TextBlurRange> ranges = textBlurRangeRepository.findByPostIdOrderByStartIdxAsc(postId);
        if (ranges.isEmpty()) {
            return  text;       // 블러없는 글은 그대로 통과 (파이프라인 규칙)
        }

        StringBuilder sb = new StringBuilder(text);
        for (TextBlurRange range : ranges) {
            // 글 수정으로 본문이 짧아졌을 수 있으니 항상 길이로 방어
            int start = Math.min(range.getStartIdx(), sb.length());
            int end = Math.min(range.getEndIdx(), sb.length());
            for (int i = start; i < end; i++) {
                sb.setCharAt(i, BLUR_CHAR);
            }
        }
        return  sb.toString();

    }


    /** extensions.textBlur 칸의 JSON 형태 */
    record RangesPayload(List<Range> ranges) {
        record Range(int start, int end) {}
    }

}
