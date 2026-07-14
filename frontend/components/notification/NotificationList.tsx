import type { AppNotification } from "@/types";
import styles from "./NotificationList.module.css";

interface NotificationListProps {
  notifications: AppNotification[];
  onItemClick?: (notification: AppNotification) => void;
}

export default function NotificationList({ notifications, onItemClick }: NotificationListProps) {
  return (
    <ul className={styles.list}>
      {notifications.map((n) => (
        <li
          key={n.id}
          className={`${styles.item} ${!n.isRead ? styles.unread : ""}`}
          onClick={() => onItemClick?.(n)}
        >
          {n.message}
        </li>
      ))}
    </ul>
  );
}
