// 내 팔로잉/팔로워 목록. TODO: GET /api/v1/follows/me/following, /me/followers
import styles from "./page.module.css";

export default function MyFollowPage() {
  return (
    <main className={styles.container}>
      <h1 className={styles.title}>팔로우</h1>
      <div className={styles.tabs}>{/* TODO: 팔로잉 / 팔로워 탭 */}</div>
      <div className={styles.list}>{/* TODO */}</div>
    </main>
  );
}
