import styles from "./page.module.css";

// 홈 피드: 팔로우/채널 기반 게시글 목록 (그림/글/영상 혼합).
// TODO: GET /api/v1/posts (검색/필터/정렬 파라미터 포함) 호출 후 아래 3가지 카드 패턴으로 렌더링
export default function HomePage() {
  return (
    <div className={styles.feed}>
      {/* 협업 캔버스(그림) 게시글 */}
      <article className={styles.card}>
        <div className={styles.cardHeader}>
          <div className={styles.author}>
            <div className={styles.avatar} />
            <div>
              <p className={styles.authorName}>디지털캔버스 콜렉티브</p>
              <p className={styles.liveStatus}>
                <span className={`${styles.liveDot} presencePulse`} />
                실시간 협업 중
              </p>
            </div>
          </div>
          <div className={styles.collaboratorStack}>
            <span />
            <span />
            <span className={styles.moreBadge}>+5</span>
          </div>
        </div>

        <div className={styles.mediaWrap}>
          <div className={styles.mediaImg} />
          <div className={`${styles.joinBadge} glass`}>
            <span className="material-symbols-outlined filled" style={{ fontSize: 16 }}>groups</span>
            참여 중...
          </div>
        </div>

        <div className={styles.cardBody}>
          <div className={styles.actionRow}>
            <button type="button"><span className="material-symbols-outlined">favorite</span></button>
            <button type="button"><span className="material-symbols-outlined">mode_comment</span></button>
            <button type="button"><span className="material-symbols-outlined">share</span></button>
            <button type="button" className={styles.spacerLeft}><span className="material-symbols-outlined">bookmark</span></button>
          </div>
          <p className={styles.caption}>
            <strong>디지털캔버스</strong> 최근 스프린트에서 네오 도쿄 미학을 탐구하고 있습니다. 참여하여 당신의 감각을 더해보세요!
          </p>
        </div>
      </article>

      {/* 문서(글) 게시글 - 일부만 공개되고 나머지는 페이드 처리 */}
      <article className={styles.card}>
        <div className={styles.cardHeader}>
          <div className={styles.author}>
            <div className={styles.avatarPlain} />
            <div>
              <p className={styles.authorName}>엘레나 라이트</p>
              <p className={styles.liveStatus}>컨셉 디자인 문서 • 2시간 전</p>
            </div>
          </div>
          <button type="button" className="material-symbols-outlined">more_horiz</button>
        </div>

        <div className={styles.docBox}>
          <h3 className={styles.docTitle}>플로우의 철학</h3>
          <p className={`${styles.docText} textFade`}>
            디지털 영역에서 인터페이스는 무의식적 표현의 통로 역할을 합니다. 전통적인 네비게이션의 마찰을 제거할 때,
            아티스트는 도구와 마음이 하나가 되는 &lsquo;하이퍼 플로우&rsquo; 상태에 진입합니다. 이 문서는 어떻게...
          </p>
          <div className={styles.readMoreWrap}>
            <button className={styles.readMoreBtn} type="button">전체 문서 읽기</button>
          </div>
        </div>

        <div className={styles.docStats}>
          <span className={styles.docStatItem}><span className="material-symbols-outlined" style={{ fontSize: 20 }}>thumb_up</span>124 좋아요</span>
          <span className={styles.docStatItem}><span className="material-symbols-outlined" style={{ fontSize: 20 }}>comment</span>18 댓글</span>
        </div>
      </article>

      {/* 영상 게시글 */}
      <article className={styles.card}>
        <div className={styles.cardHeader}>
          <div className={styles.author}>
            <div className={styles.avatarPlain} />
            <div>
              <p className={styles.authorName}>모션마스터</p>
              <p className={styles.liveStatus}>VFX 분석</p>
            </div>
          </div>
        </div>

        <div className={styles.mediaWrapSquare}>
          <div className={styles.mediaImg} style={{ opacity: 0.8 }} />
          <div className={styles.playOverlay}>
            <div className={`${styles.playButton} glass`}>
              <span className="material-symbols-outlined filled" style={{ fontSize: 48 }}>play_arrow</span>
            </div>
          </div>
          <div className={styles.videoMeta}>
            <span className={`${styles.pill} glass`}>0:15</span>
            <span className={styles.livePill}>
              <span className="material-symbols-outlined filled" style={{ fontSize: 14 }}>sensors</span>
              실시간
            </span>
          </div>
        </div>

        <div className={styles.cardBody}>
          <div className={styles.actionRow}>
            <button type="button"><span className="material-symbols-outlined">favorite</span></button>
            <button type="button"><span className="material-symbols-outlined">mode_comment</span></button>
            <button type="button"><span className="material-symbols-outlined">share</span></button>
          </div>
          <p className={styles.caption}><strong>키네틱 유동성: 에피소드 4</strong></p>
          <p className={styles.caption}>새로운 4nf 엔진을 사용한 실시간 액체 시뮬레이션입니다. 정말 놀랍네요.</p>
        </div>
      </article>
    </div>
  );
}
