import styles from "./page.module.css";

// 게시글 상세: 구독 여부에 따라 콘텐츠를 다르게 표시하는 핵심 화면.
// TODO: GET /api/v1/posts/{id} 호출 -> locked, visibility 값에 따라 아래 블러/블랙박스/부분공개 UI 분기
// (지금은 Stitch 프로토타입 그대로 고정 데이터로 3가지 잠금 패턴을 모두 보여준다)
export default async function PostDetailPage({ params }: { params: Promise<{ id: string }> }) {
  const { id } = await params;

  return (
    <div className={styles.container}>
      <article className={styles.card}>
        <div className={styles.cardInner}>
          <div className={styles.header}>
            <div>
              <h1 className={styles.title}>하지(Summer Solstice) 시리즈</h1>
              <p className={styles.byline}>
                By <strong>Elena Vance</strong> • 2시간 전 · #{id}
              </p>
            </div>
            <div className={styles.avatarStack}>
              <span className="presencePulse" />
              <span />
              <span />
            </div>
          </div>

          {/* 이미지 그리드: 전체공개 1 + 구독자 전용(BLUR) 1 + 독점 콘텐츠(BLACKBOX) 1 */}
          <section className={styles.mediaGrid}>
            <div className={styles.mainMedia}>
              <span className={`${styles.publicTag} glass`}>공개</span>
            </div>

            <div className={styles.restrictedCluster}>
              <div className={styles.restrictedItem}>
                <div className={`${styles.lockOverlay} contentRestrictionBlur`}>
                  <span className="material-symbols-outlined filled" style={{ fontSize: 36 }}>lock</span>
                  <p className={styles.lockTitle}>구독자 전용</p>
                  <p className={styles.lockDesc}>고해상도 마스터클래스 에셋을 잠금 해제하세요.</p>
                  {/* TODO: POST /api/v1/subscriptions 로 이동 */}
                  <button className={styles.joinBtn} type="button">세션 참여</button>
                </div>
              </div>
              <div className={styles.restrictedItem}>
                <div className={`${styles.lockOverlay} contentRestrictionBlur`}>
                  <span className="material-symbols-outlined filled" style={{ fontSize: 24 }}>lock</span>
                  <p className={styles.lockDescSm}>독점 콘텐츠</p>
                </div>
              </div>
            </div>
          </section>

          {/* 문서(글) 섹션: 부분공개(PARTIAL) - 일부 문단 정상, 일부 문단 redaction */}
          <section className={styles.docSection}>
            <div className={styles.docHeader}>
              <h3 className={styles.docTitle}>제작 노트: 빛의 이론</h3>
              <div className={styles.docMeta}>
                <span className="material-symbols-outlined" style={{ fontSize: 16 }}>description</span>
                초안 V2.4 • 대외비
              </div>
            </div>

            <div className={styles.docBody}>
              <p>
                이 프로젝트의 핵심 철학은 빛을 물리적 매체로 다루는 것에 있습니다. 당사는 자체 제작 엔진에서 볼륨
                스캐터링(volumetric scattering)을 실험하며, 가상 환경에서 햇빛이 미세 입자와 어떻게 상호 작용하는지에
                집중하는 것으로 시작했습니다.
              </p>

              <div className={styles.redactedWrap}>
                <p className={styles.redactedText}>
                  Our proprietary algorithm for subsurface scattering is the result of years of research. We found
                  that by modulating the frequency of the light waves based on the density of the virtual fabric,
                  we could achieve a level of realism previously unseen in digital art.
                </p>
                <div className={`${styles.redactedFloating}`}>
                  <div className={`${styles.redactedPanel} glass`}>
                    <p className={styles.redactedPanelText}>비공개 콘텐츠</p>
                    {/* TODO: 구독 페이지로 이동 */}
                    <button className={styles.subscribeCta} type="button">구독하여 확인하기</button>
                  </div>
                </div>
              </div>

              <p>
                최종 결과물의 예술적 완성도를 보장하기 위해 여백의 미를 우선시했습니다. 구도가 &ldquo;숨을 쉴 수&rdquo;
                있게 함으로써 보는 이의 시선이 고강도 영역으로 자연스럽게 이끌리며, 캔버스를 가로지르는 리드미컬한
                시각적 여정을 만들어냅니다.
              </p>

              <div className={styles.largeRedactedBlock}>
                <div className={styles.redactedLines}>
                  <div className={styles.redactedLine} style={{ width: "100%" }} />
                  <div className={styles.redactedLine} style={{ width: "92%" }} />
                  <div className={styles.redactedLine} style={{ width: "83%" }} />
                  <div className={styles.redactedLine} style={{ width: "100%" }} />
                </div>
                <div className={styles.upgradeOverlay}>
                  <div className={styles.upgradeInner}>
                    <span className="material-symbols-outlined filled" style={{ color: "var(--color-primary)" }}>workspace_premium</span>
                    <p className={styles.upgradeLabel}>기술 사양 제한됨</p>
                    <button className={styles.upgradeBtn} type="button">더 읽으려면 프로로 업그레이드</button>
                  </div>
                </div>
              </div>
            </div>
          </section>
        </div>
      </article>
    </div>
  );
}
