import styles from "./page.module.css";

// 채널 목록/탐색. TODO: 채널 목록 API 연동
export default function ChannelListPage() {
  return (
    <main className={styles.container}>
      <div className={styles.header}>
        <h1 className={styles.title}>채널</h1>
      </div>
      <div className={styles.grid}>{/* TODO: components/channel/ChannelCard 목록 */}</div>
    </main>
  );
}
