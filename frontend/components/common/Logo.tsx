import styles from "./Logo.module.css";

// 4NF 브랜드 로고 (배지 + 워드마크). 파비콘(app/icon.svg)과 동일한 아이덴티티.
// light=true 면 사진/어두운 배경 위에서 워드마크를 흰색으로 표시한다.
export default function Logo({ light = false }: { light?: boolean }) {
  return (
    <span className={light ? `${styles.logo} ${styles.light}` : styles.logo}>
      <span className={styles.mark}>4NF</span>
      <span className={styles.type}>
        <span className={light ? `${styles.title} ${styles.titleLight}` : `${styles.title} brand-gradient-text`}>
          404
        </span>
        <span className={styles.sub}>Not Found</span>
      </span>
    </span>
  );
}
