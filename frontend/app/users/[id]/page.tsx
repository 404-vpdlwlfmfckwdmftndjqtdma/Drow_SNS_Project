import styles from "./page.module.css";

// 다른 사용자 프로필 + 팔로우 버튼 + 해당 사용자의 게시글 목록.
// TODO: GET /api/v1/users/{id}, POST/DELETE /api/v1/follows/{id}
export default async function UserProfilePage({ params }: { params: Promise<{ id: string }> }) {
  const { id } = await params;
  return (
    <main className={styles.container}>
      <div className={styles.header}>
        <h1 className={styles.nickname}>사용자 #{id}</h1>
        {/* TODO: 팔로우/언팔로우 버튼 */}
      </div>
      <div className={styles.grid}>{/* TODO: 게시글 목록 */}</div>
    </main>
  );
}
