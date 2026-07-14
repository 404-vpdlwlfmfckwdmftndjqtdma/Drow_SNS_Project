"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import api from "@/lib/api";
import type { ApiResponse, User } from "@/types";
import styles from "./page.module.css";

// 마이페이지: 프로필(닉네임/소개/이미지는 실제 연동) + 게시글 썸네일 포트폴리오.
// 팔로워 목록/진행중 협업 사이드바, 그림/문서/숏폼 탭은 제거하고 그만큼 썸네일 영역을 넓게 쓴다.
// 프로필 수정 버튼은 이름 오른쪽 끝에 붙여서 아래 포트폴리오 영역과 간격을 둔다.
// TODO: 팔로워/창작물/조회수 등 통계는 GET /api/v1/mypage 집계 API(2.1) 완성 후 연동, 포트폴리오 목록도 GET /api/v1/posts?authorId=me 로 연동
export default function MyPage() {
  const router = useRouter();
  const [me, setMe] = useState<User | null>(null);

  useEffect(() => {
    (async () => {
      try {
        const res = await api.get<ApiResponse<User>>("/api/v1/users/me");
        setMe(res.data.data);
      } catch {
        // 비로그인 등으로 실패하면 프로필 영역은 빈 상태로 둔다.
      }
    })();
  }, []);

  return (
    <div className={styles.container}>
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
            <div>
              <span className={styles.statValue} style={{ color: "var(--color-primary)" }}>1.2k</span>
              <span className={styles.statLabel}>팔로워</span>
            </div>
            <div className={styles.divider} />
            <div>
              <span className={styles.statValue} style={{ color: "var(--color-secondary)" }}>850</span>
              <span className={styles.statLabel}>창작물</span>
            </div>
            <div className={styles.divider} />
            <div>
              <span className={styles.statValue} style={{ color: "var(--color-tertiary)" }}>12.5M</span>
              <span className={styles.statLabel}>조회수</span>
            </div>
          </div>
        </div>
      </div>

      <div className={styles.portfolioGrid}>
        <div className={`${styles.portfolioCard} ${styles.portfolioCardWide}`}>
          <div className={styles.portfolioMediaWide} />
        </div>

        <div className={styles.portfolioCard}>
          <div className={styles.portfolioMedia} />
          <div className={styles.portfolioBody}>
            <h3 className={styles.portfolioTitle}>Geometric Study #12</h3>
            <div className={styles.portfolioMetaRow}>
              <span className={styles.likeMeta}>
                <span className="material-symbols-outlined" style={{ fontSize: 18 }}>favorite</span>1.2k
              </span>
              <span className={styles.publicBadge}>공개</span>
            </div>
          </div>
        </div>

        {/* 비공개 초안 - 잠금 오버레이 */}
        <div className={styles.portfolioCard}>
          <div className={styles.portfolioMedia}>
            <div className={styles.lockOverlay}>
              <span className="material-symbols-outlined" style={{ fontSize: 32 }}>lock</span>
              <p className={styles.lockTitle}>Private Draft</p>
              <p className={styles.lockDesc}>비공개 초안입니다</p>
            </div>
          </div>
          <div className={styles.portfolioBody}>
            <h3 className={styles.portfolioTitle}>Project: Synthesis</h3>
            <span className={styles.likeMeta}>
              <span className="material-symbols-outlined" style={{ fontSize: 18 }}>visibility_off</span>숨김
            </span>
          </div>
        </div>
      </div>
    </div>
  );
}
