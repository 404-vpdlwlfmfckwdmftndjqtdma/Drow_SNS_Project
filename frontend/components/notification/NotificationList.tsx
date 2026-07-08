import type { AppNotification } from "@/types";

export default function NotificationList({ notifications }: { notifications: AppNotification[] }) {
  return (
    <ul>
      {notifications.map((n) => (
        <li key={n.id}>{n.message}</li>
      ))}
    </ul>
  );
}
