import styles from "./page.module.css";

// 알림 목록 + 읽음 처리. TODO: GET /api/v1/notifications, PATCH /api/v1/notifications/{id}/read
export default function NotificationsPage() {
  return (
    <main className={styles.container}>
      <h1 className={styles.title}>알림</h1>
      {/* TODO: components/notification/NotificationList */}
    </main>
  );
}
