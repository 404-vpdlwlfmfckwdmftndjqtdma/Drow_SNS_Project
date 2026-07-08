import styles from "./page.module.css";

// 채널 상세 + 채널 소속 게시글 피드. TODO: GET /api/v1/channels/{id}
export default async function ChannelDetailPage({ params }: { params: Promise<{ id: string }> }) {
  const { id } = await params;
  return (
    <main className={styles.container}>
      <div className={styles.header}>
        <h1 className={styles.title}>채널 #{id}</h1>
        {/* TODO: 채널 추가/해지 버튼 */}
      </div>
      <div className={styles.grid}>{/* TODO: 채널 게시글 피드 */}</div>
    </main>
  );
}
