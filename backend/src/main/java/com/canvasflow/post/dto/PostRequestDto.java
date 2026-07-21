package com.canvasflow.post.dto;

import com.canvasflow.global.common.ContentVisibility;
import com.canvasflow.global.media.MediaType;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

//가공 전 데이터 받는 dto

public record PostRequestDto(
        String content,
        ContentVisibility visibility,
        int requiresdlevel,                 // <- 추가 LOCKED일 때 요구 등급)
        BigDecimal singlePurchasePrice,     // (null = 단건 구매 불가)
        List<String> tags,
        List<MediaItem> media,

        // 다른 모듈(textBlur, imageBlur 등)이 자기 데이터를 꺼내갈 상자.
        // key = 모듈 이름(PostExtension.key()), value = 그 모듈만 아는 데이터.
        // post는 내용물을 모르고 그대로 각 모듈에 전달만 한다.
        Map<String, Object> extensions,

        // 판매자가 정한 기능별 잠금 해제 가격. key = capability(=PostExtension.key()), value = 원 단위 가격.
        // 예: { "textBlur": 5000, "imageBlur": 3000 }  → 안 넣으면 판매 안 함(구독자만 열람).
        Map<String, BigDecimal> prices
) {

    public record MediaItem(
            String url,
            MediaType mediaType
    ) {
    }
}

/* 매핑 예시
    {
  "content": "...",
  "visibility": "PUBLIC",
  "tags": ["일상"],
  "media": [
    { "url": "https://...", "mediaType": "IMAGE" }
  ],
  "extensions": {
    "textBlur": { "ranges": [...] }
  }
}
 */
