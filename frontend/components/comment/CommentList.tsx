import styles from "./CommentList.module.css";

interface CommentListItem {
  id: number;
  content: string;
  writerId?: number;
  writerNickname?: string;
  createdAt?: string;
}

export default function CommentList({ comments }: { comments: CommentListItem[] }) {
  return (
    <ul className={styles.list}>
      {comments.map((c) => (
        <li key={c.id} className={styles.item}>
          <p className={styles.meta}>
            {c.writerNickname ?? (c.writerId != null ? `유저 ${c.writerId}` : "익명")}
            {c.createdAt ? ` · ${new Date(c.createdAt).toLocaleString()}` : ""}
          </p>
          {c.content}
        </li>
      ))}
    </ul>
  );
}
