import styles from "../account-list.module.css";

export default function MyCommentsPage() {
  return (
    <main className={styles.container}>
      <header className={styles.header}>
        <p className={styles.eyebrow}>MY ACTIVITY</p>
        <h1 className={styles.title}>댓글</h1>
        <p className={styles.description}>내가 작성한 댓글과 대화를 모아봅니다.</p>
      </header>

      <section className={styles.emptyState}>
        <span className={styles.iconWrap} aria-hidden="true">
          <span className="material-symbols-outlined">chat_bubble</span>
        </span>
        <h2 className={styles.emptyTitle}>작성한 댓글이 없습니다</h2>
        <p className={styles.emptyDescription}>
          내 댓글 목록 조회 API가 연결되면 이곳에 댓글이 표시됩니다.
        </p>
      </section>
    </main>
  );
}
