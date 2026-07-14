import styles from "./page.module.css";

// 다른 사용자 프로필(= 구 "채널 상세") + 팔로우/친구추가 버튼 + 게시글 포트폴리오.
// 내 마이페이지와 마찬가지로 팔로워 목록 사이드바 / 그림·문서·숏폼 탭은 없애고, 그만큼 포트폴리오 영역을 넓게 쓴다.
// 팔로우/친구 추가 버튼은 이름 오른쪽 끝에 붙인다(내 마이페이지의 "프로필 수정" 자리와 동일한 위치).
// 가장 최근 게시글은 2칸 폭(portfolioCardWide)으로 크게 노출한다 - 실제 게시글 API 연동 시
// 최신순으로 정렬한 첫 항목이 이 자리에 오도록 교체하면 된다.
// TODO: GET /api/v1/mypage/{id}(프로필+카운트), POST/DELETE /api/v1/follows/{id} 연동
export default async function UserProfilePage({ params }: { params: Promise<{ id: string }> }) {
  const { id } = await params;

  return (
    <div className={styles.container}>
      <section className={styles.cover} />

      <div className={styles.profileRow}>
        <div className={styles.avatarWrap}>
          <div className={styles.onlineDot} />
        </div>
        <div className={styles.profileInfo}>
          <div className={styles.nameRow}>
            <h1 className={styles.name}>Elena Rivers</h1>
            <div className={styles.actions}>
              {/* TODO: POST /api/v1/follows/{userId} */}
              <button className={styles.primaryBtn} type="button">
                <span className="material-symbols-outlined filled" style={{ fontSize: 20 }}>person_add</span>
                팔로우
              </button>
              <button className={styles.glassBtn} type="button">
                <span className="material-symbols-outlined" style={{ fontSize: 20 }}>add</span>
                친구 추가
              </button>
            </div>
          </div>
          <p className={styles.bio}>
            다양한 분야를 넘나드는 디자이너로서 생성 예술과 인간 감정의 교차점을 탐구합니다. @캔버스플로우 콜렉티브의 수석 큐레이터입니다.
            <span style={{ display: "none" }}>{id}</span>
          </p>
          <div className={styles.stats}>
            <div>
              <span className={styles.statValue} style={{ color: "var(--color-primary)" }}>12.4k</span>
              <span className={styles.statLabel}>팔로워</span>
            </div>
            <div className={styles.divider} />
            <div>
              <span className={styles.statValue} style={{ color: "var(--color-secondary)" }}>842</span>
              <span className={styles.statLabel}>창작물</span>
            </div>
            <div className={styles.divider} />
            <div>
              <span className={styles.statValue} style={{ color: "var(--color-tertiary)" }}>1.2M</span>
              <span className={styles.statLabel}>조회수</span>
            </div>
          </div>
        </div>
      </div>

      <div className={styles.portfolioGrid}>
        {/* 가장 최근 게시글 - 2칸 폭으로 크게 노출 */}
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
