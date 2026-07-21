import type { AppNotification } from "@/types";
import styles from "./NotificationList.module.css";

interface NotificationListProps {
  notifications: AppNotification[];
  onItemClick?: (notification: AppNotification) => void;
  selectMode?: boolean;
  selectedIds?: Set<number>;
  onToggleSelect?: (id: number) => void;
}

export default function NotificationList({
  notifications,
  onItemClick,
  selectMode = false,
  selectedIds,
  onToggleSelect,
}: NotificationListProps) {
  return (
    <ul className={styles.list}>
      {notifications.map((n) => {
        const selected = selectedIds?.has(n.id) ?? false;
        return (
          <li
            key={n.id}
            className={`${styles.item} ${!n.isRead ? styles.unread : ""} ${selected ? styles.selected : ""}`}
            onClick={() => (selectMode ? onToggleSelect?.(n.id) : onItemClick?.(n))}
          >
            {selectMode && (
              <input
                type="checkbox"
                className={styles.checkbox}
                checked={selected}
                onChange={() => onToggleSelect?.(n.id)}
                onClick={(e) => e.stopPropagation()}
              />
            )}
            <span className={styles.message}>{n.message}</span>
          </li>
        );
      })}
    </ul>
  );
}
