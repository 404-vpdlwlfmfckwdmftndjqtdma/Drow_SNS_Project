import styles from "./page.module.css";

// 검색 결과 페이지 (제목/내용 키워드). TODO: GET /api/v1/search?keyword=
export default function SearchPage() {
  return (
    <main className={styles.container}>
      <div className={styles.searchBar}>
        <input className={styles.input} type="text" placeholder="검색어를 입력하세요" />
      </div>
      <div className={styles.grid}>{/* TODO: 검색 결과 목록 */}</div>
    </main>
  );
}
