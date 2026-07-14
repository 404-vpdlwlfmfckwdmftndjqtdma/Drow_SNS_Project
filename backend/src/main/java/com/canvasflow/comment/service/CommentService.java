package com.canvasflow.comment.service;

import com.canvasflow.comment.dto.CommentCreateRequest;
import com.canvasflow.comment.dto.CommentDeletedEvent;
import com.canvasflow.comment.dto.CommentResponse;
import com.canvasflow.comment.dto.CommentUpdateRequest;
import com.canvasflow.comment.entity.Comment;
import com.canvasflow.comment.repository.CommentRepository;
import com.canvasflow.comment.sse.CommentEmitterRepository;
import com.canvasflow.global.exception.CanvasflowException;
import com.canvasflow.global.exception.ErrorCode;
import com.canvasflow.like.entity.Like;
import com.canvasflow.like.entity.LikeTargetType;
import com.canvasflow.like.repository.LikeRepository;
import com.canvasflow.notification.entity.NotificationTargetType;
import com.canvasflow.notification.entity.NotificationType;
import com.canvasflow.notification.service.NotificationService;
import com.canvasflow.post.repository.PostRepository;
import com.canvasflow.user.entity.User;
import com.canvasflow.user.repository.UserRepository;
import com.canvasflow.user.UserFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 댓글 CRUD + 1-depth 대댓글 트리 조립을 담당.
 * post_id/writer_id는 다른 모듈 소유 데이터라 FK 없이 ID만 저장하므로, 여기서 검증하지 않는 한
 * 존재하지 않는 post_id로도 댓글이 생길 수 있다 (의도된 설계 - post 존재 검증은 이번 스코프 제외).
 */
@RequiredArgsConstructor
@Service
public class CommentService {

    // 프록시/브라우저가 무응답 커넥션을 끊는 걸 막기 위한 하트비트 주기
    private static final long HEARTBEAT_INTERVAL_MS = 30_000;
    // 클라이언트가 재연결(EventSource 기본 재시도)로 이어받을 수 있으니 넉넉하게 잡는다
    private static final long SUBSCRIBE_TIMEOUT_MS = 30 * 60 * 1000L;

    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final LikeRepository likeRepository;
    private final PostRepository postRepository;
    private final NotificationService notificationService;
    private final CommentEmitterRepository commentEmitterRepository;
    private final UserFacade userFacade;

    @Transactional
    public CommentResponse create(Long userId, Long postId, CommentCreateRequest request) {
        Long parentId = request.parentId();
        Comment parent = null;
        if (parentId != null) {
            parent = commentRepository.findById(parentId)
                    .orElseThrow(() -> new CanvasflowException(ErrorCode.COMMENT_NOT_FOUND));
            if (parent.isReply()) {
                throw new CanvasflowException(ErrorCode.INVALID_INPUT_VALUE); // 대댓글의 대댓글 금지
            }
            if (!parent.getPostId().equals(postId)) {
                throw new CanvasflowException(ErrorCode.INVALID_INPUT_VALUE);
            }
        }

        String writerNickname = userFacade.getNicknameOrThrow(userId);

        Comment comment = commentRepository.save(Comment.builder()
                .postId(postId)
                .parentId(parentId)
                .writerId(userId)
                .content(request.content())
                .build());

        return CommentResponse.of(comment, writerNickname, List.of());
    }

    // 원댓글만 페이징하고, 그 페이지에 뜬 원댓글들의 대댓글은 전부(페이징 없이) 붙여서 내려준다.
    // 대댓글까지 한 목록으로 플랫 페이징하면 스레드가 페이지 경계에서 끊길 수 있어 이렇게 나눔.
    // viewerId는 로그인 없이 조회 가능하므로 null일 수 있다 (이 경우 likedByMe는 항상 false).
    @Transactional(readOnly = true)
    public Page<CommentResponse> getComments(Long postId, Long viewerId, Pageable pageable) {
        Page<Comment> roots = commentRepository.findByPostIdAndParentIdIsNullOrderByCreatedAtAsc(postId, pageable);

        // 대댓글을 원댓글 개수만큼 개별 조회하면 N+1이 나므로, ID 목록으로 한 번에 조회
        List<Long> rootIds = roots.getContent().stream().map(Comment::getId).toList();
        List<Comment> replies = rootIds.isEmpty()
                ? List.of()
                : commentRepository.findByParentIdInOrderByCreatedAtAsc(rootIds);

        Map<Long, List<Comment>> repliesByParentId = replies.stream()
                .collect(Collectors.groupingBy(Comment::getParentId));
        Map<Long, String> nicknames = fetchNicknames(roots.getContent(), replies);

        List<Long> allCommentIds = Stream.concat(roots.getContent().stream(), replies.stream())
                .map(Comment::getId)
                .toList();
        List<Like> likes = allCommentIds.isEmpty()
                ? List.of()
                : likeRepository.findByTargetTypeAndTargetIdIn(LikeTargetType.COMMENT, allCommentIds);
        Map<Long, Long> likeCounts = likes.stream()
                .collect(Collectors.groupingBy(Like::getTargetId, Collectors.counting()));
        Set<Long> likedByViewer = viewerId == null
                ? Set.of()
                : likes.stream()
                        .filter(like -> like.getUserId().equals(viewerId))
                        .map(Like::getTargetId)
                        .collect(Collectors.toSet());

        return roots.map(root -> {
            List<CommentResponse> replyResponses = repliesByParentId
                    .getOrDefault(root.getId(), List.of())
                    .stream()
                    .map(reply -> CommentResponse.of(
                            reply,
                            nicknames.get(reply.getWriterId()),
                            likeCounts.getOrDefault(reply.getId(), 0L),
                            likedByViewer.contains(reply.getId()),
                            List.of()))
                    .toList();
            return CommentResponse.of(
                    root,
                    nicknames.get(root.getWriterId()),
                    likeCounts.getOrDefault(root.getId(), 0L),
                    likedByViewer.contains(root.getId()),
                    replyResponses);
        });
    }

    @Transactional
    public CommentResponse update(Long commentId, Long userId, CommentUpdateRequest request) {
        Comment comment = getOwnedActiveComment(commentId, userId);
        comment.changeContent(request.content());
        String nickname = userFacade.findNicknameById(userId);
        return CommentResponse.of(comment, nickname, List.of());
    }

    // 삭제된(DELETED) 원댓글에도 새 대댓글이 계속 달릴 수 있으므로(스레드 맥락 유지),
    // 삭제 시점에 자식이 없어도 이후에 생길 수 있다 - 그래서 하드삭제는 "지금 이 순간" 기준으로만 판단한다.
    @Transactional
    public void delete(Long commentId, Long userId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CanvasflowException(ErrorCode.COMMENT_NOT_FOUND));
        if (!comment.getWriterId().equals(userId)) {
            throw new CanvasflowException(ErrorCode.FORBIDDEN);
        }

        Long postId = comment.getPostId();
        boolean hardDeleted = !commentRepository.existsByParentId(commentId);
        if (hardDeleted) {
            commentRepository.delete(comment); // 자식 없으면 하드삭제
        } else {
            comment.softDelete(); // 대댓글이 달려있으면 상태만 변경 (자식이 부모를 잃지 않게)
        }
        broadcast(postId, "comment-deleted", new CommentDeletedEvent(commentId, hardDeleted));
    }

    public SseEmitter subscribe(Long postId) {
        SseEmitter emitter = new SseEmitter(SUBSCRIBE_TIMEOUT_MS);
        commentEmitterRepository.save(postId, emitter);
        emitter.onCompletion(() -> commentEmitterRepository.remove(postId, emitter));
        emitter.onTimeout(() -> commentEmitterRepository.remove(postId, emitter));
        emitter.onError(e -> commentEmitterRepository.remove(postId, emitter));

        try {
            // 연결 직후 더미 이벤트를 보내야 일부 프록시/브라우저가 커넥션을 바로 확정한다.
            emitter.send(SseEmitter.event().name("connect").data("connected"));
        } catch (IOException e) {
            commentEmitterRepository.remove(postId, emitter);
        }
        return emitter;
    }

    @Scheduled(fixedRate = HEARTBEAT_INTERVAL_MS)
    public void sendHeartbeat() {
        for (SseEmitter emitter : commentEmitterRepository.findAll()) {
            try {
                emitter.send(SseEmitter.event().name("heartbeat").data("ping"));
            } catch (IOException e) {
                emitter.complete();
            }
        }
    }

    private void broadcast(Long postId, String eventName, Object data) {
        for (SseEmitter emitter : commentEmitterRepository.findByPostId(postId)) {
            try {
                emitter.send(SseEmitter.event().name(eventName).data(data));
            } catch (IOException e) {
                commentEmitterRepository.remove(postId, emitter);
            }
        }
    }

    // 수정은 삭제된(DELETED) 댓글엔 허용하지 않는다 - "삭제된 댓글입니다" 표시만 있는 껍데기를
    // 다시 채워넣는 게 의미가 없어서, 존재하지 않는 것처럼 COMMENT_NOT_FOUND로 처리.
    private Comment getOwnedActiveComment(Long commentId, Long userId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CanvasflowException(ErrorCode.COMMENT_NOT_FOUND));
        if (!comment.getWriterId().equals(userId)) {
            throw new CanvasflowException(ErrorCode.FORBIDDEN);
        }
        if (comment.isDeleted()) {
            throw new CanvasflowException(ErrorCode.COMMENT_NOT_FOUND);
        }
        return comment;
    }

    private Map<Long, String> fetchNicknames(List<Comment> roots, List<Comment> replies) {
        List<Long> writerIds = Stream.concat(roots.stream(), replies.stream())
                .map(Comment::getWriterId)
                .distinct()
                .toList();
        return userFacade.findNicknamesByIds(writerIds);
    }
}
