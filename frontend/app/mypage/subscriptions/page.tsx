// 내가 구독 중인 채널/작가 및 구독 등급 목록. TODO: GET /api/v1/subscriptions/me
import styles from "./page.module.css";

export default function MySubscriptionsPage() {
  return (
    <main className={styles.container}>
      <h1 className={styles.title}>구독</h1>
      <div className={styles.list}>{/* TODO */}</div>
    </main>
  );
}
