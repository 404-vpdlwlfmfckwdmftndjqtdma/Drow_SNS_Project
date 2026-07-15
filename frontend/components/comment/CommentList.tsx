import styles from "./CommentList.module.css";

interface CommentListItem {
  id: number;
  content: string;
  writerId?: number;
  writerNickname?: string;
  createdAt?: string;
}

interface CommentListProps {
  comments: CommentListItem[];
  currentUserId?: number | null;
}

export default function CommentList({ comments, currentUserId = null }: CommentListProps) {
  return (
    <ul className={styles.list}>
      {comments.map((c) => (
        <li key={c.id} className={styles.item}>
          <p className={styles.meta}>
            <span className={c.writerId != null && c.writerId === currentUserId ? styles.nicknameMine : styles.nickname}>
              {c.writerNickname ?? (c.writerId != null ? `유저 ${c.writerId}` : "익명")}
            </span>
            {c.createdAt ? ` · ${new Date(c.createdAt).toLocaleString()}` : ""}
          </p>
          {c.content}
        </li>
      ))}
    </ul>
  );
}
