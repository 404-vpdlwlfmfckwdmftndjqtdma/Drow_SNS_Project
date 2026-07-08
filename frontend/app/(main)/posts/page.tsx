import styles from "./page.module.css";

// 게시글 목록 (검색/필터/정렬 쿼리스트링 반영).
// TODO: GET /api/v1/posts?keyword=&channelId=&contentType=&tag=&sort=&page= 호출
export default function PostListPage() {
  return (
    <main className={styles.container}>
      <div className={styles.header}>
        <h1 className={styles.title}>게시글</h1>
      </div>
      <div className={styles.filters}>{/* TODO: 콘텐츠 타입/채널/태그 필터, 정렬 셀렉트 */}</div>
      <div className={styles.grid}>{/* TODO: components/post/PostCard 목록 */}</div>
    </main>
  );
}
