"use client";

import { useEffect, useState } from "react";
import Link from "next/link";
import { useParams } from "next/navigation";
import api from "@/lib/api";
import FollowButton from "@/components/follow/FollowButton";
import PortfolioGrid, { toPortfolioPosts, type MyPagePostResponse, type PortfolioPost } from "@/components/post/PortfolioGrid";
import type { ApiResponse } from "@/types";
import styles from "./page.module.css";

// 백엔드 MyPageResponse 와 1:1로 맞춘 응답 타입 (본인/타인 마이페이지 공용).
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

// 다른 사용자 프로필(= 구 "채널 상세") + 팔로우 버튼 + 게시글 포트폴리오.
// 포트폴리오 그리드는 마이페이지와 공용인 PortfolioGrid 컴포넌트를 쓴다 - 사진/영상이 있으면 첫 썸네일만,
// 없으면 텍스트만 보여주고 좋아요 수는 노출하지 않는다.
// GET /api/v1/mypage/{id}/posts (PostReader.getPostsByAuthorId 기반, 최신 작성순) 로 실제 게시글 연동.
// 팔로우 버튼은 이름 오른쪽 끝에 붙인다(내 마이페이지의 "프로필 수정" 자리와 동일한 위치).
// 팔로우 버튼 자체(상태 조회/토글/확인 모달)는 components/follow/FollowButton으로 분리했다 -
// 구조가 바뀌어도(채널=팔로잉 목록 등) targetUserId만 넘기면 어디서든 재사용 가능.
// GET /api/v1/mypage/{id} (프로필+카운트, 로그인 불필요) 연동.
// 통계 4개: 팔로워/팔로잉/창작물/조회수 전부 FollowFacade/PostReader 기반 실수치.
export default function UserProfilePage() {
  const { id } = useParams<{ id: string }>();
  const targetUserId = Number(id);

  const [profile, setProfile] = useState<MyPageSummary | null>(null);
  const [loading, setLoading] = useState(true);
  const [posts, setPosts] = useState<PortfolioPost[]>([]);
  const [postsLoading, setPostsLoading] = useState(true);

  useEffect(() => {
    if (!Number.isFinite(targetUserId)) return;

    (async () => {
      try {
        const res = await api.get<ApiResponse<MyPageSummary>>(`/api/v1/mypage/${targetUserId}`);
        setProfile(res.data.data);
      } catch {
        // 실패해도 화면은 유지한다 (프로필 영역만 빈 상태로 남김).
      } finally {
        setLoading(false);
      }
    })();

    (async () => {
      try {
        const res = await api.get<ApiResponse<MyPagePostResponse[]>>(`/api/v1/mypage/${targetUserId}/posts`);
        setPosts(toPortfolioPosts(res.data.data));
      } catch {
        // 실패하면 빈 목록으로 둔다.
      } finally {
        setPostsLoading(false);
      }
    })();
  }, [targetUserId]);

  const handleFollowChange = (following: boolean) => {
    setProfile((prev) =>
      prev ? { ...prev, followerCount: Math.max(0, prev.followerCount + (following ? 1 : -1)) } : prev
    );
  };

  return (
    <div className={styles.container}>
      <section className={styles.cover} />

      <div className={styles.profileRow}>
        <div
          className={styles.avatarWrap}
          style={
            profile?.profileImageUrl
              ? { backgroundImage: `url(${profile.profileImageUrl})`, backgroundSize: "cover", backgroundPosition: "center" }
              : undefined
          }
        >
          <div className={styles.onlineDot} />
        </div>
        <div className={styles.profileInfo}>
          <div className={styles.nameRow}>
            <h1 className={styles.name}>{profile?.nickname ?? (loading ? "불러오는 중..." : "알 수 없는 사용자")}</h1>
            <div className={styles.actions}>
              <FollowButton targetUserId={targetUserId} onFollowChange={handleFollowChange} />
            </div>
          </div>
          <p className={styles.bio}>
            {profile?.bio || "소개가 아직 없습니다."}
          </p>
          <div className={styles.stats}>
            <Link href={`/users/${targetUserId}/followers`} className={styles.statLink}>
              <span className={styles.statValue} style={{ color: "var(--color-primary)" }}>
                {profile?.followerCount ?? 0}
              </span>
              <span className={styles.statLabel}>팔로워</span>
            </Link>
            <div className={styles.divider} />
            <div>
              <span className={styles.statValue} style={{ color: "var(--color-secondary)" }}>
                {profile?.followingCount ?? 0}
              </span>
              <span className={styles.statLabel}>팔로잉</span>
            </div>
            <div className={styles.divider} />
            <div>
              <span className={styles.statValue} style={{ color: "var(--color-tertiary)" }}>
                {profile?.postCount ?? 0}
              </span>
              <span className={styles.statLabel}>창작물</span>
            </div>
            <div className={styles.divider} />
            <div>
              <span className={styles.statValue} style={{ color: "var(--color-primary)" }}>
                {(profile?.viewCount ?? 0).toLocaleString()}
              </span>
              <span className={styles.statLabel}>조회수</span>
            </div>
          </div>
        </div>
      </div>

      {!postsLoading && <PortfolioGrid wideFirst posts={posts} isOwner={false} />}
    </div>
  );
}
