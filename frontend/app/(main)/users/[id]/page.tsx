"use client";

import { useEffect, useState } from "react";
import { useParams } from "next/navigation";
import api from "@/lib/api";
import FollowButton from "@/components/follow/FollowButton";
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
}

// 다른 사용자 프로필(= 구 "채널 상세") + 팔로우 버튼 + 게시글 포트폴리오.
// 내 마이페이지와 마찬가지로 팔로워 목록 사이드바 / 그림·문서·숏폼 탭은 없애고, 그만큼 포트폴리오 영역을 넓게 쓴다.
// 팔로우 버튼은 이름 오른쪽 끝에 붙인다(내 마이페이지의 "프로필 수정" 자리와 동일한 위치).
// 팔로우 버튼 자체(상태 조회/토글/확인 모달)는 components/follow/FollowButton으로 분리했다 -
// 구조가 바뀌어도(채널=팔로잉 목록 등) targetUserId만 넘기면 어디서든 재사용 가능.
// GET /api/v1/mypage/{id} (프로필+카운트, 로그인 불필요) 연동.
// 통계 4개: 팔로워/팔로잉은 FollowFacade 기반 실수치, 창작물(postCount)은 post 쪽 집계가 아직 없어 0으로 내려온다,
// 조회수는 MyPageResponse에 필드 자체가 없어 더미로 남겨둔다.
// TODO: 게시글 포트폴리오 4개는 여전히 더미 - GET /api/v1/posts?authorId={id} 연동 대기.
export default function UserProfilePage() {
  const { id } = useParams<{ id: string }>();
  const targetUserId = Number(id);

  const [profile, setProfile] = useState<MyPageSummary | null>(null);
  const [loading, setLoading] = useState(true);

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
            <div>
              <span className={styles.statValue} style={{ color: "var(--color-primary)" }}>
                {profile?.followerCount ?? 0}
              </span>
              <span className={styles.statLabel}>팔로워</span>
            </div>
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
              <span className={styles.statValue} style={{ color: "var(--color-primary)" }}>1.2M</span>
              <span className={styles.statLabel}>조회수</span>
            </div>
          </div>
        </div>
      </div>

      <div className={styles.portfolioGrid}>
        {/* 가장 최근 게시글 - 2칸 폭으로 크게 노출 (더미) */}
        <div className={`${styles.portfolioCard} ${styles.portfolioCardWide}`}>
          <div className={styles.portfolioMediaWide} />
          <div className={styles.portfolioBody}>
            <h3 className={styles.portfolioTitle}>신경망 유동성</h3>
            <div className={styles.portfolioMetaRow}>
              <span className={styles.likeMeta}>
                <span className="material-symbols-outlined" style={{ fontSize: 18 }}>favorite</span>1.2k
              </span>
              <span className={styles.publicBadge}>공개</span>
            </div>
          </div>
        </div>

        <div className={styles.portfolioCard}>
          <div className={styles.portfolioMedia}>
            <div className={styles.lockOverlay}>
              <span className="material-symbols-outlined" style={{ fontSize: 32 }}>lock</span>
              <p className={styles.lockTitle}>비공개 걸작</p>
              <p className={styles.lockDesc}>이 캔버스를 보려면 액세스 요청이 필요합니다</p>
            </div>
          </div>
          <div className={styles.portfolioBody}>
            <h3 className={styles.portfolioTitle}>프로젝트: 에테리아</h3>
            <span className={styles.likeMeta}>
              <span className="material-symbols-outlined" style={{ fontSize: 18 }}>visibility_off</span>숨김
            </span>
          </div>
        </div>

        <div className={styles.docCard}>
          <div>
            <div className={styles.docCardHeader}>
              <span className="material-symbols-outlined" style={{ color: "var(--color-tertiary)" }}>article</span>
              <span className={styles.likeMeta}>초안</span>
            </div>
            <h3 className={styles.docCardTitle}>공생 선언서</h3>
            <p className={styles.docCardText}>
              이 문서는 창의적 협업에서 일어나는 근본적인 변화를 다룹니다. AI가 도구가 아닌 동료로 자리 잡을 때 생기는
              변화를 탐구하며, 저작권의 경계와 디지털 공동체 의식에 대해 논의합니다...
            </p>
          </div>
          <div className={styles.docCardFooter}>
            <span>2일 전 수정됨</span>
            <button className={styles.moreBtn} type="button">더 보기</button>
          </div>
        </div>

        <div className={styles.portfolioCard}>
          <div className={styles.shortMediaWrap}>
            <div className={styles.shortOverlayPlay}>
              <span className="material-symbols-outlined" style={{ fontSize: 64 }}>play_circle</span>
            </div>
            <span className={styles.liveTag}>
              <span className="material-symbols-outlined filled" style={{ fontSize: 12 }}>sensors</span>라이브
            </span>
            <div className={styles.shortCaption}>
              <p style={{ fontWeight: 700, fontSize: 14 }}>심야 스케치 #042</p>
              <p style={{ fontSize: 12, opacity: 0.8 }}>8.4k 시청 중</p>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
