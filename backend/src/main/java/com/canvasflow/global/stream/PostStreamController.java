package com.canvasflow.global.stream;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Set;

/**
 * 피드 전용 단일 SSE 구독 진입점.
 * 클라이언트는 화면에 보이는 postId 집합을 넘기고, 서버는 그 postId들 이벤트만 팬아웃한다.
 */
@RequiredArgsConstructor
@RestController
public class PostStreamController {

    private final PostStreamService postStreamService;

    @GetMapping(value = "/api/v1/posts/stream/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe(@RequestParam Set<Long> postIds) {
        return postStreamService.subscribe(postIds);
    }
}
