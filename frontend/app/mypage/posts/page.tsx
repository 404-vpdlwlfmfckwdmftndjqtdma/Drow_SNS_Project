import styles from "./page.module.css";

// 내가 작성한 게시글 목록. TODO: GET /api/v1/posts?authorId=me 형태 API 연동
export default function MyPostsPage() {
  return (
    <main className={styles.container}>
      <h1 className={styles.title}>내 게시글</h1>
      <div className={styles.grid}>{/* TODO */}</div>
    </main>
  );
}
