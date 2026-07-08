import styles from "./page.module.css";

// 게시글 상세. locked=true 인 경우 components/subscription/ContentGate 로
// 공개 범위(블러/블랙박스/접근제한/부분공개)에 맞춰 마스킹 UI를 보여준다.
// TODO: GET /api/v1/posts/{id} 호출
export default async function PostDetailPage({ params }: { params: Promise<{ id: string }> }) {
  const { id } = await params;
  return (
    <main className={styles.container}>
      <h1 className={styles.title}>게시글 #{id}</h1>
      <div className={styles.meta}>{/* TODO: 작성자, 조회수, 작성일 */}</div>
      <div className={styles.content}>{/* TODO: ContentGate 로 감싼 본문/미디어 */}</div>
      <div className={styles.actions}>{/* TODO: 좋아요, 댓글, 구독 버튼 */}</div>
    </main>
  );
}
