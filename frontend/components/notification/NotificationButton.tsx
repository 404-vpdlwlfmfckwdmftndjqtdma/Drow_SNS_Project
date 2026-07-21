"use client";

import { useEffect, useRef, useState } from "react";
import { useRouter } from "next/navigation";
import api from "@/lib/api";
import { subscribeToNotifications } from "@/lib/notificationStream";
import NotificationDropdown from "./NotificationDropdown";
import type { AppNotification, ApiResponse, PageResponse } from "@/types";
import styles from "./NotificationButton.module.css";

interface NotificationButtonProps {
  userId: number;
}

// 알림을 클릭했을 때 이동할 경로. 댓글(COMMENT)은 단독 페이지가 없어서 백엔드가 애초에
// targetType=POST로 내려준다 - 여기서는 POST/USER 두 가지만 실제로 매칭된다.
function resolveHref(notification: AppNotification): string | null {
  switch (notification.targetType) {
    case "POST":
      return `/posts/${notification.targetId}`;
    case "USER":
      return `/users/${notification.targetId}`;
    default:
      return null;
  }
}

/**
 * [notification 버튼 모듈] 태그로 꽂아 쓰는 실시간 알림 벨 버튼.
 * SSE로 안 읽은 개수를 실시간으로 갱신하고, 클릭하면 벨 아래 드롭다운으로 목록을 띄운다.
 */
export default function NotificationButton({ userId }: NotificationButtonProps) {
  const router = useRouter();
  const [open, setOpen] = useState(false);
  const [notifications, setNotifications] = useState<AppNotification[]>([]);
  const [unreadCount, setUnreadCount] = useState(0);
  const [selectMode, setSelectMode] = useState(false);
  const [selectedIds, setSelectedIds] = useState<Set<number>>(new Set());
  const wrapRef = useRef<HTMLDivElement>(null);

  // 드롭다운이 닫히면 선택 모드도 같이 초기화한다.
  useEffect(() => {
    if (!open) {
      setSelectMode(false);
      setSelectedIds(new Set());
    }
  }, [open]);

  useEffect(() => {
    fetchInitial();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [userId]);

  // 드롭다운 바깥을 클릭하면 닫는다 (Sidebar 프로필 풀다운과 동일한 패턴).
  useEffect(() => {
    if (!open) return;
    function close(e: MouseEvent) {
      if (wrapRef.current && !wrapRef.current.contains(e.target as Node)) {
        setOpen(false);
      }
    }
    document.addEventListener("mousedown", close);
    return () => document.removeEventListener("mousedown", close);
  }, [open]);

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
      // 읽음 처리 실패는 알림 이동 자체를 막을 정도는 아니라 조용히 무시한다.
    }
  }

  // 좋아요/댓글/답글 알림 → 그 게시글로, 팔로우 알림 → 그 사람 프로필로 이동한다.
  function handleItemClick(notification: AppNotification) {
    setOpen(false);
    markAsRead(notification);
    const href = resolveHref(notification);
    if (href) router.push(href);
  }

  function toggleSelectMode() {
    setSelectMode((v) => !v);
    setSelectedIds(new Set());
  }

  function toggleSelect(id: number) {
    setSelectedIds((prev) => {
      const next = new Set(prev);
      if (next.has(id)) {
        next.delete(id);
      } else {
        next.add(id);
      }
      return next;
    });
  }

  async function deleteSelected() {
    if (selectedIds.size === 0) return;
    const ids = [...selectedIds];
    const deletedUnreadCount = notifications.filter((n) => selectedIds.has(n.id) && !n.isRead).length;

    try {
      await api.delete("/api/v1/notifications", { data: ids });
    } catch {
      alert("알림 삭제에 실패했습니다.");
      return;
    }

    setNotifications((prev) => prev.filter((n) => !selectedIds.has(n.id)));
    setUnreadCount((prev) => Math.max(0, prev - deletedUnreadCount));
    setSelectMode(false);
    setSelectedIds(new Set());
  }

  return (
    <div className={styles.wrap} ref={wrapRef}>
      <button type="button" className={styles.bell} onClick={() => setOpen((v) => !v)} aria-label="알림">
        <span className="material-symbols-outlined">notifications</span>
        {unreadCount > 0 && <span className={styles.badge}>{unreadCount > 99 ? "99+" : unreadCount}</span>}
      </button>
      {open && (
        <NotificationDropdown
          notifications={notifications}
          onItemClick={handleItemClick}
          onClose={() => setOpen(false)}
          selectMode={selectMode}
          selectedIds={selectedIds}
          onToggleSelectMode={toggleSelectMode}
          onToggleSelect={toggleSelect}
          onDeleteSelected={deleteSelected}
        />
      )}
    </div>
  );
}
