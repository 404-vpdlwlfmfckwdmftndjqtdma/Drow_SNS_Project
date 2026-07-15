"use client";

import { useEffect, useState } from "react";
import api from "@/lib/api";
import { subscribeToPostFeed } from "@/lib/postFeedStream";
import styles from "./PostLikeButton.module.css";

interface ApiEnvelope<T> {
  success: boolean;
  message?: string;
  data: T;
}

interface LikeResult {
  liked: boolean;
  likeCount: number;
}

interface PostLikeButtonProps {
  postId: number;
  userId: number | null;
  initialLikeCount?: number;
}

/**
 * [post 좋아요 버튼 모듈] 태그로 꽂아 쓰는 재사용 게시글 좋아요 버튼.
 * like 도메인의 범용 API(/api/v1/likes/{targetType}/{targetId})를 targetType=POST로 호출한다.
 */
export default function PostLikeButton({ postId, userId, initialLikeCount = 0 }: PostLikeButtonProps) {
  const [liked, setLiked] = useState(false);
  const [likeCount, setLikeCount] = useState(initialLikeCount);

  useEffect(() => {
    setLikeCount(initialLikeCount);
  }, [initialLikeCount]);

  useEffect(() => {
    if (userId == null) {
      setLiked(false);
      return;
    }

    api
      .get<ApiEnvelope<LikeResult>>(`/api/v1/likes/POST/${postId}`)
      .then((res) => {
        setLiked(res.data.data.liked);
        setLikeCount(res.data.data.likeCount);
      })
      .catch(() => {
        // 초기 조회 실패는 조용히 무시 - 버튼은 계속 0/false로 동작 가능
      });
  }, [postId, userId]);

  // 이 게시글을 보고 있는 다른 사람이 좋아요를 누르거나 취소하면 실시간으로 개수만 갱신한다
  // (liked는 "나"의 상태라 브로드캐스트 대상이 아니고, 직접 누를 때만 바뀐다).
  useEffect(() => {
    return subscribeToPostFeed(postId, { onLikeCount: setLikeCount });
  }, [postId]);

  async function toggleLike() {
    if (userId == null) {
      alert("좋아요는 로그인 후 사용할 수 있습니다.");
      return;
    }

    try {
      const res = await api.request<ApiEnvelope<LikeResult>>({
        method: liked ? "delete" : "post",
        url: `/api/v1/likes/POST/${postId}`,
      });
      setLiked(res.data.data.liked);
      setLikeCount(res.data.data.likeCount);
    } catch {
      alert("좋아요 처리에 실패했습니다.");
    }
  }

  return (
    <button type="button" className={styles.likeBtn} onClick={toggleLike}>
      <span className={`material-symbols-outlined${liked ? " filled" : ""}`} style={{ fontSize: 20 }}>
        favorite
      </span>
      {likeCount}
    </button>
  );
}
