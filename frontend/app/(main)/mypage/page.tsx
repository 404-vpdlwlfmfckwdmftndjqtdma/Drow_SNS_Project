"use client";

import { useEffect, useState } from "react";
import Link from "next/link";
import { useRouter } from "next/navigation";
import api from "@/lib/api";
import CompactChannelList, { type CompactChannelItem } from "@/components/channel/list/CompactChannelList";
import PortfolioGrid, { toPortfolioPosts, type MyPagePostResponse, type PortfolioPost } from "@/components/post/PortfolioGrid";
import type { ApiResponse } from "@/types";
import styles from "./page.module.css";

// 백엔드 MyPageResponse 와 1:1로 맞춘 응답 타입 (users/[id] 페이지와 동일한 형태를 공용으로 씀).
interface MyPageSummary {
  userId: number;
  nickname: string;
  profileImageUrl?: string;
  bio?: string;
  postCount: number;
  followingCount: number;
  followerCount: number;
  subscriptionCount: number;
  viewCount: number;
}

// FollowUserResponse 와 1:1로 맞춘 응답 타입 (channels/page.tsx와 동일).
interface FollowingUser {
  userId: number;
  nickname: string;
  profileImageUrl?: string;
  bio?: string;
}

const CHANNEL_PANEL_LIMIT = 5;

// 마이페이지: 프로필(닉네임/소개/이미지/팔로워·팔로잉·창작물·조회수는 실제 연동) + 게시글 포트폴리오 그리드
//            + 우측 채널 미리보기 패널(CompactChannelList, 1200px 이상에서만 노출).
// 포트폴리오 그리드는 users/[id] 페이지와 공용인 PortfolioGrid 컴포넌트를 쓴다 - 사진/영상이 있으면 첫 썸네일만,
// 없으면 텍스트만 보여주고 좋아요 수는 노출하지 않는다.
// GET /api/v1/mypage/posts (PostReader.getPostsByAuthorId 기반, 최신 작성순) 로 실제 게시글 연동.
// 채널 미리보기 패널은 GET /api/v1/follows/following(최신 팔로우순 정렬)을 그대로 받아 앞에서 5개만 잘라서 넘긴다.
// GET /api/v1/mypage 로 연동 - 팔로워/팔로잉/창작물(postCount)/조회수(viewCount) 전부 FollowFacade/PostReader 기반 실수치.
export default function MyPage() {
  const router = useRouter();
  const [me, setMe] = useState<MyPageSummary | null>(null);
  const [channelPreview, setChannelPreview] = useState<CompactChannelItem[]>([]);
  const [posts, setPosts] = useState<PortfolioPost[]>([]);
  const [postsLoading, setPostsLoading] = useState(true);

  useEffect(() => {
    (async () => {
      try {
        const res = await api.get<ApiResponse<MyPageSummary>>("/api/v1/mypage");
        setMe(res.data.data);
      } catch {
        // 비로그인 등으로 실패하면 프로필 영역은 빈 상태로 둔다.
      }
    })();

    (async () => {
      try {
        const res = await api.get<ApiResponse<FollowingUser[]>>("/api/v1/follows/following");
        const mapped: CompactChannelItem[] = res.data.data.slice(0, CHANNEL_PANEL_LIMIT).map((user) => ({
          id: user.userId,
          name: user.nickname,
          description: user.bio,
          profileImageUrl: user.profileImageUrl,
        }));
        setChannelPreview(mapped);
      } catch {
        // 비로그인 등으로 실패하면 빈 목록으로 둔다 (더미로 되돌아가지 않도록 [] 유지).
      }
    })();

    (async () => {
      try {
        const res = await api.get<ApiResponse<MyPagePostResponse[]>>("/api/v1/mypage/posts");
        setPosts(toPortfolioPosts(res.data.data));
      } catch {
        // 비로그인 등으로 실패하면 빈 목록으로 둔다.
      } finally {
        setPostsLoading(false);
      }
    })();
  }, []);

  return (
    <div className={styles.container}>
      <div className={styles.content}>
        <section className={styles.cover} />

        <div className={styles.profileRow}>
          <div
            className={styles.avatarWrap}
            style={me?.profileImageUrl ? { backgroundImage: `url(${me.profileImageUrl})`, backgroundSize: "cover", backgroundPosition: "center" } : undefined}
          >
            <div className={styles.onlineDot} />
          </div>
          <div className={styles.profileInfo}>
            <div className={styles.nameRow}>
              <h1 className={styles.name}>{me?.nickname ?? "불러오는 중..."}</h1>
              <button
                className={styles.primaryBtn}
                type="button"
                onClick={() => router.push("/mypage/profile")}
              >
                <span className="material-symbols-outlined filled" style={{ fontSize: 20 }}>edit</span>
                프로필 수정
              </button>
            </div>
            <p className={styles.bio}>
              {me?.bio || "소개가 아직 없습니다."}
            </p>
            <div className={styles.stats}>
              {me ? (
                <Link href={`/users/${me.userId}/followers`} className={styles.statLink}>
                  <span className={styles.statValue} style={{ color: "var(--color-primary)" }}>
                    {me.followerCount}
                  </span>
                  <span className={styles.statLabel}>팔로워</span>
                </Link>
              ) : (
                <div>
                  <span className={styles.statValue} style={{ color: "var(--color-primary)" }}>0</span>
                  <span className={styles.statLabel}>팔로워</span>
                </div>
              )}
              <div className={styles.divider} />
              <div>
                <span className={styles.statValue} style={{ color: "var(--color-secondary)" }}>
                  {me?.followingCount ?? 0}
                </span>
                <span className={styles.statLabel}>팔로잉</span>
              </div>
              <div className={styles.divider} />
              <div>
                <span className={styles.statValue} style={{ color: "var(--color-tertiary)" }}>
                  {me?.postCount ?? 0}
                </span>
                <span className={styles.statLabel}>창작물</span>
              </div>
              <div className={styles.divider} />
              <div>
                <span className={styles.statValue} style={{ color: "var(--color-primary)" }}>
                  {(me?.viewCount ?? 0).toLocaleString()}
                </span>
                <span className={styles.statLabel}>조회수</span>
              </div>
            </div>
          </div>
        </div>

        {!postsLoading && <PortfolioGrid wideFirst isOwner posts={posts} />}
      </div>

      <aside className={styles.channelPanel}>
        <CompactChannelList channels={channelPreview} />
      </aside>
    </div>
  );
}
