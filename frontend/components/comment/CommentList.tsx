import type { Comment } from "@/types";
import styles from "./CommentList.module.css";

export default function CommentList({ comments }: { comments: Comment[] }) {
  return (
    <ul className={styles.list}>
      {comments.map((c) => (
        <li key={c.id} className={styles.item}>
          {c.content}
        </li>
      ))}
    </ul>
  );
}
