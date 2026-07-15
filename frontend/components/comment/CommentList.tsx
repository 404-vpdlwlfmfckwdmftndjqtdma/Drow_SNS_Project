import styles from "./CommentList.module.css";

interface CommentListItem {
  id: number;
  content: string;
}

export default function CommentList({ comments }: { comments: CommentListItem[] }) {
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
