import styles from "./page.module.css";

// 다른 사람들 목록/탐색 (구 "채널 목록" - 채널은 별도 엔티티가 아니라 각 유저의 마이페이지를 보는 것이므로,
// 여기서는 유저 목록을 보여주고 클릭하면 /users/{id}로 이동한다).
// TODO: 유저 목록 조회 API(GET /api/v1/users 등, 페이지네이션) 연동
export default function ChannelListPage() {
  return (
    <main className={styles.container}>
      <div className={styles.header}>
        <h1 className={styles.title}>채널</h1>
      </div>
      <div className={styles.grid}>{/* TODO: 유저 카드 목록, 클릭 시 /users/{id}로 이동 */}</div>
    </main>
  );
}
