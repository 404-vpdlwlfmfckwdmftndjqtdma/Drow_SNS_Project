"use client";

import { useEffect, useState } from "react";
import api from "@/lib/api";
import { subscribeToNotifications } from "@/lib/notificationStream";
import NotificationList from "@/components/notification/NotificationList";
import type { AppNotification, ApiResponse, PageResponse } from "@/types";
import styles from "./page.module.css";

// 알림 목록 + 읽음 처리 + SSE 실시간 수신.
// 목록/읽음 API는 Authorization 토큰 기반으로 동작하고,
// 구독 API는 EventSource 제약으로 userId 쿼리 파라미터를 사용한다.
export default function NotificationsPage() {
  const [userId, setUserId] = useState(1);
  const [notifications, setNotifications] = useState<AppNotification[]>([]);
  const [connected, setConnected] = useState(false);

  useEffect(() => {
    fetchNotifications();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [userId]);

  useEffect(() => {
    const unsubscribe = subscribeToNotifications(
      userId,
      (notification) => setNotifications((prev) => [notification, ...prev]),
      setConnected
    );
    return () => {
      setConnected(false);
      unsubscribe();
    };
  }, [userId]);

  async function fetchNotifications() {
    try {
      const res = await api.get<ApiResponse<PageResponse<AppNotification>>>("/api/v1/notifications", {
        params: { size: 50 },
      });
      setNotifications(res.data.data.content);
    } catch {
      alert("알림을 불러오지 못했습니다.");
    }
  }

  async function markAsRead(notification: AppNotification) {
    if (notification.isRead) return;
    try {
      await api.patch(`/api/v1/notifications/${notification.id}/read`);
      setNotifications((prev) =>
        prev.map((n) => (n.id === notification.id ? { ...n, isRead: true } : n))
      );
    } catch {
      alert("읽음 처리에 실패했습니다.");
    }
  }

  return (
    <main className={styles.container}>
      <h1 className={styles.title}>알림</h1>

      <div className={styles.row}>
        <label>userId (SSE subscribe query)</label>
        <input
          className={styles.input}
          type="number"
          value={userId}
          onChange={(e) => setUserId(Number(e.target.value))}
        />
        <span className={connected ? styles.connected : styles.disconnected}>
          {connected ? "실시간 연결됨" : "연결 끊김"}
        </span>
      </div>

      {notifications.length === 0 ? (
        <p className={styles.empty}>알림이 없습니다.</p>
      ) : (
        <NotificationList notifications={notifications} onItemClick={markAsRead} />
      )}
    </main>
  );
}
