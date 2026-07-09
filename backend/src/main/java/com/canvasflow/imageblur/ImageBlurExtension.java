package com.canvasflow.imageblur;

import com.canvasflow.post.PostExtension;
import org.springframework.stereotype.Component;

/**
 * [이미지 블러 모듈] textblur 와 같은 패턴. 담당자가 자기 테이블/저장 로직을 채운다.
 */
@Component
public class ImageBlurExtension implements PostExtension {

    @Override
    public String key() {
        return "imageBlur";
    }

    @Override
    public void apply(Long postId, Object section) {
        if (section == null) {
            return;
        }
        // TODO: section = {"targetImageIds":[...]} 파싱 → 자기 테이블에 (postId, imageId) 저장 + 블러본 생성
    }
}
