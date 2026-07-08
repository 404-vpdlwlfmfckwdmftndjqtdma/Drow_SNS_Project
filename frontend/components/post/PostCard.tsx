import type { PostSummary } from "@/types";
import styles from "./PostCard.module.css";

// 목록/피드에 쓰이는 게시글 카드. locked=true 면 썸네일을 블러 처리.
export default function PostCard({ post }: { post: PostSummary }) {
  return (
    <article className={`${styles.card} ${post.locked ? styles.locked : ""}`}>
      <div className={styles.thumbnail} />
      <div className={styles.body}>
        <h3 className={styles.title}>{post.title}</h3>
        <div className={styles.meta}>
          {/* TODO: 좋아요/댓글/조회수 */}
        </div>
      </div>
    </article>
  );
}
