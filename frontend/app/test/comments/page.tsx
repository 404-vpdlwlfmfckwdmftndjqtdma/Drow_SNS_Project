"use client";

import { useState } from "react";
import api from "@/lib/api";
import styles from "./page.module.css";

// 댓글 모듈만 따로 떼서 API를 직접 두드려보는 테스트 페이지.
// 실제 앱 화면(Header/BottomNav 등)과 분리된 독립 라우트.
// auth 도메인 JWT 로그인이 아직 안 되는 상태라(refresh_tokens 테이블 미비), 다른 도메인들과 동일하게
// X-User-Id 헤더로 사용자를 직접 지정한다 (userId 입력창에 DB에 실제 존재하는 유저 id를 넣으면 됨).
// 백엔드 comment API 설계: POST/GET /api/v1/posts/{postId}/comments, PUT/DELETE /api/v1/comments/{id}

interface CommentItem {
  id: number;
  postId: number;
  parentId: number | null;
  writerId: number;
  writerNickname?: string;
  content: string;
  deleted: boolean;
  createdAt: string;
  replies: CommentItem[];
}

interface ApiEnvelope<T> {
  success: boolean;
  message?: string;
  data: T;
}

interface PageEnvelope<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}

export default function CommentModuleTestPage() {
  const [postId, setPostId] = useState(1);
  const [userId, setUserId] = useState(1);
  const [comments, setComments] = useState<CommentItem[]>([]);
  const [rootContent, setRootContent] = useState("");
  const [replyDrafts, setReplyDrafts] = useState<Record<number, string>>({});
  const [editDrafts, setEditDrafts] = useState<Record<number, string>>({});
  const [loading, setLoading] = useState(false);
  const [log, setLog] = useState<string[]>([]);

  function pushLog(line: string) {
    setLog((prev) => [`${new Date().toLocaleTimeString()} ${line}`, ...prev].slice(0, 30));
  }

  function describeError(err: unknown): string {
    if (err && typeof err === "object" && "response" in err) {
      const res = (err as { response?: { status?: number; data?: unknown } }).response;
      return `HTTP ${res?.status} ${JSON.stringify(res?.data)}`;
    }
    return String(err);
  }

  async function fetchComments() {
    setLoading(true);
    try {
      const res = await api.get<ApiEnvelope<PageEnvelope<CommentItem>>>(
        `/api/v1/posts/${postId}/comments`,
        { params: { size: 50 } }
      );
      setComments(res.data.data.content);
      pushLog(`GET comments 성공 (${res.data.data.content.length}개 원댓글)`);
    } catch (err) {
      pushLog(`GET comments 실패: ${describeError(err)}`);
    } finally {
      setLoading(false);
    }
  }

  function authHeaders() {
    return { "X-User-Id": String(userId) };
  }

  async function createComment(content: string, parentId?: number) {
    if (!content.trim()) return;
    try {
      const res = await api.post<ApiEnvelope<CommentItem>>(
        `/api/v1/posts/${postId}/comments`,
        { content, parentId },
        { headers: authHeaders() }
      );
      pushLog(`POST comment 성공 (id=${res.data.data.id}${parentId ? `, parentId=${parentId}` : ""})`);
      await fetchComments();
    } catch (err) {
      pushLog(`POST comment 실패: ${describeError(err)}`);
    }
  }

  async function updateComment(id: number, content: string) {
    if (!content.trim()) return;
    try {
      await api.put(`/api/v1/comments/${id}`, { content }, { headers: authHeaders() });
      pushLog(`PUT comment 성공 (id=${id})`);
      await fetchComments();
    } catch (err) {
      pushLog(`PUT comment 실패: ${describeError(err)}`);
    }
  }

  async function deleteComment(id: number) {
    try {
      await api.delete(`/api/v1/comments/${id}`, { headers: authHeaders() });
      pushLog(`DELETE comment 성공 (id=${id})`);
      await fetchComments();
    } catch (err) {
      pushLog(`DELETE comment 실패: ${describeError(err)}`);
    }
  }

  function renderComment(c: CommentItem, depth: number) {
    const replyDraft = replyDrafts[c.id] ?? "";
    const editDraft = editDrafts[c.id];

    return (
      <div key={c.id} className={styles.comment} style={{ marginLeft: depth * 24 }}>
        <div className={styles.meta}>
          #{c.id} · writerId={c.writerId} · {c.deleted ? "[DELETED]" : c.createdAt}
        </div>

        {editDraft === undefined ? (
          <p className={styles.content}>{c.deleted ? "삭제된 댓글입니다" : c.content}</p>
        ) : (
          <div className={styles.row}>
            <input
              className={styles.input}
              value={editDraft}
              onChange={(e) => setEditDrafts((prev) => ({ ...prev, [c.id]: e.target.value }))}
            />
            <button onClick={() => updateComment(c.id, editDraft).then(() => setEditDrafts((prev) => {
              const next = { ...prev };
              delete next[c.id];
              return next;
            }))}>
              저장
            </button>
            <button
              onClick={() =>
                setEditDrafts((prev) => {
                  const next = { ...prev };
                  delete next[c.id];
                  return next;
                })
              }
            >
              취소
            </button>
          </div>
        )}

        <div className={styles.actions}>
          {depth === 0 && (
            <button
              onClick={() =>
                setReplyDrafts((prev) => ({ ...prev, [c.id]: prev[c.id] ?? "" }))
              }
            >
              답글
            </button>
          )}
          {!c.deleted && editDraft === undefined && (
            <button onClick={() => setEditDrafts((prev) => ({ ...prev, [c.id]: c.content }))}>수정</button>
          )}
          <button onClick={() => deleteComment(c.id)}>삭제</button>
        </div>

        {c.id in replyDrafts && depth === 0 && (
          <div className={styles.row}>
            <input
              className={styles.input}
              placeholder="대댓글 입력"
              value={replyDraft}
              onChange={(e) => setReplyDrafts((prev) => ({ ...prev, [c.id]: e.target.value }))}
            />
            <button
              onClick={() =>
                createComment(replyDraft, c.id).then(() =>
                  setReplyDrafts((prev) => {
                    const next = { ...prev };
                    delete next[c.id];
                    return next;
                  })
                )
              }
            >
              등록
            </button>
          </div>
        )}

        {c.replies?.map((r) => renderComment(r, depth + 1))}
      </div>
    );
  }

  return (
    <main className={styles.container}>
      <h1>댓글 모듈 테스트</h1>
      <p className={styles.warning}>
        JWT 로그인 대신 X-User-Id 헤더로 사용자를 지정합니다 (DB에 실제 존재하는 user id 입력).
      </p>

      <div className={styles.row}>
        <label>postId</label>
        <input
          className={styles.input}
          type="number"
          value={postId}
          onChange={(e) => setPostId(Number(e.target.value))}
        />
        <label>userId (X-User-Id)</label>
        <input
          className={styles.input}
          type="number"
          value={userId}
          onChange={(e) => setUserId(Number(e.target.value))}
        />
        <button onClick={fetchComments} disabled={loading}>
          {loading ? "조회 중..." : "댓글 조회"}
        </button>
      </div>

      <div className={styles.row}>
        <input
          className={styles.input}
          placeholder="원댓글 입력"
          value={rootContent}
          onChange={(e) => setRootContent(e.target.value)}
        />
        <button
          onClick={() => {
            createComment(rootContent);
            setRootContent("");
          }}
        >
          원댓글 등록
        </button>
      </div>

      <section className={styles.list}>
        {comments.length === 0 ? <p>댓글 없음 (조회 버튼을 눌러보세요)</p> : comments.map((c) => renderComment(c, 0))}
      </section>

      <section className={styles.logPanel}>
        <h2>요청 로그</h2>
        <ul>
          {log.map((line, i) => (
            <li key={i}>{line}</li>
          ))}
        </ul>
      </section>
    </main>
  );
}
