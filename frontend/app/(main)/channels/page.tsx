import Link from "next/link";
import styles from "./page.module.css";

// 다른 사람들 목록/탐색 (구 "채널 목록" - 채널은 별도 엔티티가 아니라 각 유저의 마이페이지를 보는 것이므로,
// 여기서는 유저 목록을 보여주고 클릭하면 /users/{id}로 이동한다).
// 지금은 "남의 마이페이지" 화면 예시를 보기 위해 카드 1개만 하드코딩해둔 상태.
// TODO: 유저 목록 조회 API(GET /api/v1/users 등, 페이지네이션) 연동 후 이 카드를 실제 목록으로 교체
export default function ChannelListPage() {
  return (
    <main className={styles.container}>
      <div className={styles.header}>
        <h1 className={styles.title}>채널</h1>
      </div>
      <div className={styles.list}>
        <div className={styles.userRow}>
          <Link href="/users/1" className={styles.userLink}>
            <div className={styles.avatar}>ER</div>
            <div className={styles.userInfo}>
              <p className={styles.userName}>Elena Rivers</p>
              <p className={styles.userBio}>생성 예술과 인간 감정의 교차점을 탐구하는 디자이너입니다.</p>
            </div>
          </Link>
          <button className={styles.followBtn} type="button">
            <span className="material-symbols-outlined" style={{ fontSize: 18 }}>person_add</span>
            팔로우
          </button>
        </div>
      </div>
    </main>
  );
}
