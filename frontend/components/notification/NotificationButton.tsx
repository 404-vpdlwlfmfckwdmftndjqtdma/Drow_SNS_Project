"use client";

import { useEffect, useState } from "react";
import api from "@/lib/api";
import { subscribeToNotifications } from "@/lib/notificationStream";
import NotificationModal from "./NotificationModal";
import type { AppNotification, ApiResponse, PageResponse } from "@/types";
import styles from "./NotificationButton.module.css";

interface NotificationButtonProps {
  userId: number;
}

/**
 * [notification 버튼 모듈] 태그로 꽂아 쓰는 실시간 알림 벨 버튼.
 * SSE로 안 읽은 개수를 실시간으로 갱신하고, 클릭하면 NotificationModal 팝업으로 목록을 띄운다.
 */
export default function NotificationButton({ userId }: NotificationButtonProps) {
  const [open, setOpen] = useState(false);
  const [notifications, setNotifications] = useState<AppNotification[]>([]);
  const [unreadCount, setUnreadCount] = useState(0);

  useEffect(() => {
    fetchInitial();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [userId]);

  useEffect(() => {
    const unsubscribe = subscribeToNotifications(userId, (notification) => {
      setNotifications((prev) => [notification, ...prev]);
      setUnreadCount((prev) => prev + 1);
    });
    return unsubscribe;
  }, [userId]);

  async function fetchInitial() {
    try {
      const [listRes, countRes] = await Promise.all([
        api.get<ApiResponse<PageResponse<AppNotification>>>("/api/v1/notifications", {
          params: { size: 20 },
        }),
        api.get<ApiResponse<number>>("/api/v1/notifications/unread-count"),
      ]);
      setNotifications(listRes.data.data.content);
      setUnreadCount(countRes.data.data);
    } catch {
      // 벨 아이콘은 부가 기능이라 초기 조회 실패로 화면을 막지 않는다
    }
  }

  async function markAsRead(notification: AppNotification) {
    if (notification.isRead) return;
    try {
      await api.patch(`/api/v1/notifications/${notification.id}/read`);
      setNotifications((prev) => prev.map((n) => (n.id === notification.id ? { ...n, isRead: true } : n)));
      setUnreadCount((prev) => Math.max(0, prev - 1));
    } catch {
      alert("읽음 처리에 실패했습니다.");
    }
  }

  return (
    <>
      <button type="button" className={styles.bell} onClick={() => setOpen(true)} aria-label="알림">
        <span className="material-symbols-outlined">notifications</span>
        {unreadCount > 0 && <span className={styles.badge}>{unreadCount > 99 ? "99+" : unreadCount}</span>}
      </button>
      {open && (
        <NotificationModal notifications={notifications} onItemClick={markAsRead} onClose={() => setOpen(false)} />
      )}
    </>
  );
}
