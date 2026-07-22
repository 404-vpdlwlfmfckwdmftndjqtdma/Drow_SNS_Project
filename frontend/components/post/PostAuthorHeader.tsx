import Link from "next/link";
import styles from "./PostAuthorHeader.module.css";

interface PostAuthorHeaderProps {
  userId: number;
  nickname?: string | null;
  profileImageUrl?: string | null;
  createdAt: string;
  className?: string;
}

export default function PostAuthorHeader({
  userId,
  nickname,
  profileImageUrl,
  createdAt,
  className,
}: PostAuthorHeaderProps) {
  const displayName = nickname?.trim() || `작성자 #${userId}`;
  const initial = displayName.charAt(0).toUpperCase();

  return (
    <Link
      className={[styles.root, className].filter(Boolean).join(" ")}
      href={`/users/${userId}`}
      aria-label={`${displayName} 채널로 이동`}
    >
      <span
        className={styles.avatar}
        style={
          profileImageUrl
            ? {
                backgroundImage: `url(${profileImageUrl})`,
                backgroundPosition: "center",
                backgroundSize: "cover",
              }
            : undefined
        }
        aria-hidden="true"
      >
        {!profileImageUrl && initial}
      </span>
      <span className={styles.text}>
        <span className={styles.authorName}>{displayName}</span>
        <time className={styles.timestamp} dateTime={createdAt}>
          {new Date(createdAt).toLocaleString()}
        </time>
      </span>
    </Link>
  );
}
