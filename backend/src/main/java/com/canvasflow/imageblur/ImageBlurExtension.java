package com.canvasflow.imageblur;

import com.canvasflow.global.media.MediaType;
import com.canvasflow.imageblur.internal.ImageBlurRepository;
import com.canvasflow.imageblur.internal.ImageBlurTarget;
import com.canvasflow.post.PostExtension;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * [이미지 블러 모듈] textblur 와 같은 패턴. 담당자가 자기 테이블/저장 로직을 채운다.
 */
@Component
@RequiredArgsConstructor
public class ImageBlurExtension implements PostExtension {

    private final ImageBlurRepository imageBlurRepository;

    @Override
    public String key() {
        return "imageBlur";
    }

    @Override
    public void apply(Long postId, Object section) {
        imageBlurRepository.deleteByPostId(postId);
        if (!(section instanceof  Map<?, ?> map) || !(map.get("targetIndexes") instanceof  List<?> indexes)) {
            return;
        }List<ImageBlurTarget> rows = new ArrayList<>();
        for(Object item : indexes){
            if(item instanceof  Number index) {
                rows.add(new ImageBlurTarget(postId, index.intValue()));
            }

        }
        imageBlurRepository.saveAll(rows);
    }

    @Override
    public List<PostExtension.MediaItem> renderMedia(Long postId, List<PostExtension.MediaItem> media){
        Set<Integer> blurred = imageBlurRepository.findByPostId(postId).stream()
                .map(ImageBlurTarget::getMediaIndex)
                .collect(Collectors.toSet());
        if(blurred.isEmpty()) return media;

        List<PostExtension.MediaItem> result = new ArrayList<>();
        for (int i = 0; i < media.size(); i++) {
            PostExtension.MediaItem item = media.get(i);
            if (blurred.contains(i) && item.mediaType() == MediaType.IMAGE) {
                result.add(new PostExtension.MediaItem(item.url().replaceFirst("/upload/", "/upload/e_blur:2000/"), item.mediaType()));
            } else {
                result.add(item);
            }
        }
        return result;
    }
}
