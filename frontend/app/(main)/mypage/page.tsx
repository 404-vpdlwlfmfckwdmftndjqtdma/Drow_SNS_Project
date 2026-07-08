import styles from "./page.module.css";

// 마이페이지: 프로필 + 그림/문서/숏폼 탭 포트폴리오 + 팔로워/진행중 협업 사이드바.
// TODO: GET /api/v1/mypage 로 요약 데이터, GET /api/v1/posts?authorId=me 로 포트폴리오 목록 연동
export default function MyPage() {
  return (
    <div className={styles.container}>
      <section className={styles.cover} />

      <div className={styles.profileRow}>
        <div className={styles.avatarWrap}>
          <div className={styles.onlineDot} />
        </div>
        <div className={styles.profileInfo}>
          <h1 className={styles.name}>이민수 (Minsoo Lee)</h1>
          <p className={styles.bio}>
            @minsoo_creates • 디지털 아트 &amp; 모션 디자이너 • 다양한 분야를 넘나드는 디자인으로 창의적인 영감을 나눕니다.
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
          <div className={styles.actions}>
            {/* TODO: /mypage/profile 로 이동 */}
            <button className={styles.primaryBtn} type="button">
              <span className="material-symbols-outlined filled" style={{ fontSize: 20 }}>edit</span>
              프로필 수정
            </button>
            <button className={styles.glassBtn} type="button">
              <span className="material-symbols-outlined" style={{ fontSize: 20 }}>settings</span>
              설정
            </button>
          </div>
        </div>
      </div>

      <div className={styles.grid}>
        <div>
          <div className={styles.tabs}>
            <button className={styles.tabActive} type="button">그림(422)</button>
            <button className={styles.tab} type="button">문서(128)</button>
            <button className={styles.tab} type="button">숏폼(292)</button>
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

        <aside>
          <div className={styles.sidebarCard}>
            <div className={styles.sidebarHeader}>
              <h2 className={styles.sidebarTitle}>팔로워</h2>
              <button className={styles.viewAll} type="button">전체 보기</button>
            </div>
            {[
              { name: "Marcus Chen", meta: "3D Sculptor • Pro" },
              { name: "Sarah Drasner", meta: "Design Engineer" },
              { name: "Alex Kim", meta: "Illustrator • 12k followers" },
            ].map((f) => (
              <div className={styles.followerItem} key={f.name}>
                <div className={styles.followerAvatar} />
                <div>
                  <p className={styles.followerName}>{f.name}</p>
                  <p className={styles.followerMeta}>{f.meta}</p>
                </div>
              </div>
            ))}
          </div>

          <div className={styles.liveCollabCard}>
            <div className={styles.sidebarHeader}>
              <h2 className={styles.followerName}>진행 중인 협업</h2>
              <span className={styles.liveBadge}>LIVE</span>
            </div>
            <p className={styles.followerName}>UI Design System v2</p>
            <p className={styles.followerMeta}>김지수, 최성훈 외 2명</p>
            <button className={styles.joinCanvasBtn} type="button">캔버스 참가하기</button>
          </div>
        </aside>
      </div>
    </div>
  );
}
