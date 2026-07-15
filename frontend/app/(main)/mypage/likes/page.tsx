import styles from "../account-list.module.css";

export default function MyLikesPage() {
  return (
    <main className={styles.container}>
      <header className={styles.header}>
        <p className={styles.eyebrow}>MY ACTIVITY</p>
        <h1 className={styles.title}>좋아요</h1>
        <p className={styles.description}>내가 좋아요한 게시물을 한곳에서 확인합니다.</p>
      </header>

      <section className={styles.emptyState}>
        <span className={styles.iconWrap} aria-hidden="true">
          <span className="material-symbols-outlined">favorite</span>
        </span>
        <h2 className={styles.emptyTitle}>좋아요한 게시물이 없습니다</h2>
        <p className={styles.emptyDescription}>
          좋아요 목록 조회 API가 연결되면 이곳에 게시물이 표시됩니다.
        </p>
      </section>
    </main>
  );
}
