import styles from "./page.module.css";

// 마이페이지 요약(프로필 + 게시글/팔로우/구독/알림 카운트).
// TODO: GET /api/v1/mypage
export default function MyPage() {
  return (
    <main className={styles.container}>
      <div className={styles.profile}>
        <div className={styles.nickname}>{/* TODO: 닉네임/프로필 이미지 */}</div>
      </div>
      <div className={styles.statGrid}>
        <div className={styles.statCard}>게시글</div>
        <div className={styles.statCard}>팔로잉</div>
        <div className={styles.statCard}>팔로워</div>
        <div className={styles.statCard}>구독</div>
      </div>
    </main>
  );
}
