import type { AppNotification } from "@/types";
import styles from "./NotificationList.module.css";

export default function NotificationList({ notifications }: { notifications: AppNotification[] }) {
  return (
    <ul className={styles.list}>
      {notifications.map((n) => (
        <li key={n.id} className={`${styles.item} ${!n.isRead ? styles.unread : ""}`}>
          {n.message}
        </li>
      ))}
    </ul>
  );
}
