"use client";

import { useState } from "react";
import styles from "./page.module.css";

// 내 팔로잉/팔로워 목록. TODO: GET /api/v1/follows/me/following, /me/followers 연동
export default function MyFollowPage() {
  const [tab, setTab] = useState<"following" | "followers">("following");

  return (
    <div className={styles.layout}>
      <div className={styles.container}>
        <h1 className={styles.title}>친구 관리</h1>

        <div className={styles.searchBox}>
          <span className="material-symbols-outlined">search</span>
          {/* TODO: 검색어로 팔로잉/팔로워 목록 필터링 */}
          <input className={styles.searchInput} type="text" placeholder="친구의 아이디나 이름을 검색하세요" />
        </div>

        <div className={styles.tabs}>
          <button className={tab === "following" ? styles.tabActive : styles.tab} onClick={() => setTab("following")} type="button">
            팔로잉 (128)
          </button>
          <button className={tab === "followers" ? styles.tabActive : styles.tab} onClick={() => setTab("followers")} type="button">
            팔로워 (256)
          </button>
        </div>

        <div className={styles.list}>
          {[
            { name: "김지수", bio: "디지털 페인터 | 몽환적인 밤하늘을 그립니다.", state: "following" as const },
            { name: "Ethan Park", bio: "UI/UX Designer & Motion Artist. Let's create together!", state: "mutual" as const },
            { name: "서유진", bio: "단편 소설 작가. 일상의 조각들을 모으는 중.", state: "following" as const },
            { name: "Minho Lee", bio: "Character Concept Artist @StudioCanvas", state: "following" as const },
          ].map((person) => (
            <div className={styles.item} key={person.name}>
              <div className={styles.avatar} />
              <div className={styles.itemBody}>
                <p className={styles.itemName}>{person.name}</p>
                <p className={styles.itemBio}>{person.bio}</p>
              </div>
              {/* TODO: DELETE /api/v1/follows/{targetUserId} 로 언팔로우 */}
              {person.state === "mutual" ? (
                <button className={styles.mutualBtn} type="button">
                  <span className="material-symbols-outlined" style={{ fontSize: 18 }}>sync</span>
                  맞팔로우
                </button>
              ) : (
                <button className={styles.followingBtn} type="button">팔로잉</button>
              )}
            </div>
          ))}
        </div>
      </div>

      <aside className={styles.rightSidebar}>
        <div className={styles.sideHeader}>
          <h2 className={styles.sideTitle}>추천 친구</h2>
          <button className={styles.viewAll} type="button">모두 보기</button>
        </div>
        {[
          { name: "Rachel Kim", meta: "3명의 아는 친구" },
          { name: "박도준", meta: "인기 일러스트레이터" },
        ].map((r) => (
          <div className={styles.suggestItem} key={r.name}>
            <div className={styles.avatar} style={{ width: 48, height: 48 }} />
            <div style={{ flex: 1 }}>
              <p className={styles.suggestName}>{r.name}</p>
              <p className={styles.suggestMeta}>{r.meta}</p>
            </div>
            <span className="material-symbols-outlined" style={{ color: "var(--color-primary)" }}>person_add</span>
          </div>
        ))}

        <h2 className={styles.sideTitle} style={{ marginTop: "var(--space-lg)" }}>인기 크리에이터</h2>
        <div className={styles.creatorGrid}>
          <div className={styles.creatorCard}>
            <div className={styles.creatorOverlay} />
            <div className={styles.creatorText}>
              <p className={styles.creatorName}>Design Master</p>
              <span className={styles.creatorFollowers}>팔로워 12k</span>
            </div>
          </div>
          <div className={styles.creatorCard}>
            <div className={styles.creatorOverlay} />
            <div className={styles.creatorText}>
              <p className={styles.creatorName}>Nature Art</p>
              <span className={styles.creatorFollowers}>팔로워 8.5k</span>
            </div>
          </div>
        </div>
      </aside>
    </div>
  );
}
