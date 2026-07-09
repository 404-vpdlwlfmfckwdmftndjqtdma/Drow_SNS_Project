package com.canvasflow.videowatermark;

import com.canvasflow.post.PostExtension;
import org.springframework.stereotype.Component;

/**
 * [영상 워터마크 모듈] textblur 와 같은 패턴. 담당자가 자기 테이블/저장 로직을 채운다.
 */
@Component
public class VideoWatermarkExtension implements PostExtension {

    @Override
    public String key() {
        return "videoWatermark";
    }

    @Override
    public void apply(Long postId, Object section) {
        if (section == null) {
            return;
        }
        // TODO: section = {"targets":[{"videoId":..,"text":".."}]} 파싱 → 자기 테이블에 저장
    }
}
