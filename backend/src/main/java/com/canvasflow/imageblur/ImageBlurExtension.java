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

    //저장 할 때
    @Override
    public void apply(Long postId, Object section) {
        imageBlurRepository.deleteByPostId(postId); //기존의 사진들을 지움
        if (!(section instanceof  Map<?, ?> map) || !(map.get("targetIndexes") instanceof  List<?> indexes)) {
            return;
        }List<ImageBlurTarget> rows = new ArrayList<>();
        //프론트에서 받은 형태 확인, 배열 돌며 가릴 사진 저장
        for(Object item : indexes){
            if(item instanceof  Number index) {
                rows.add(new ImageBlurTarget(postId, index.intValue()));
            }

        }
        imageBlurRepository.saveAll(rows);
    }

    @Override
    //화면에 보여주는 코드, 블러처리는 Cloudinary의 e_blur:숫자 활용
    public List<PostExtension.MediaItem> renderMedia(Long postId, List<PostExtension.MediaItem> media, boolean unlocked){
        if (unlocked) return media; // 구매/구독으로 잠금 해제된 뷰어에게는 원본 그대로

        Set<Integer> blurred = imageBlurRepository.findByPostId(postId).stream()
                .map(ImageBlurTarget::getMediaIndex)
                .collect(Collectors.toSet());
        if(blurred.isEmpty()) return media;

        List<PostExtension.MediaItem> result = new ArrayList<>();
        for (int i = 0; i < media.size(); i++) {
            PostExtension.MediaItem item = media.get(i);
            if (blurred.contains(i) && item.mediaType() == MediaType.IMAGE) {
                result.add(new PostExtension.MediaItem(item.url().replaceFirst("/upload/", "/upload/e_blur:3000/"), item.mediaType()));
            } else {
                result.add(item);
            }
        }
        return result;
    }
}
