"use client";

import { useEffect } from "react";
import NotificationList from "./NotificationList";
import type { AppNotification } from "@/types";
import styles from "./NotificationModal.module.css";

interface NotificationModalProps {
  notifications: AppNotification[];
  onItemClick: (notification: AppNotification) => void;
  onClose: () => void;
}

// [notification 모듈 - 팝업 콘텐츠] NotificationButton이 열어주는 알림 목록 팝업.
export default function NotificationModal({ notifications, onItemClick, onClose }: NotificationModalProps) {
  useEffect(() => {
    function handleKeyDown(e: KeyboardEvent) {
      if (e.key === "Escape") onClose();
    }
    window.addEventListener("keydown", handleKeyDown);
    return () => window.removeEventListener("keydown", handleKeyDown);
  }, [onClose]);

  return (
    <div className={styles.overlay} onClick={onClose}>
      <div className={styles.panel} onClick={(e) => e.stopPropagation()}>
        <div className={styles.header}>
          <span className={styles.title}>알림</span>
          <button className={styles.closeBtn} onClick={onClose} aria-label="닫기">
            <span className="material-symbols-outlined">close</span>
          </button>
        </div>

        <div className={styles.body}>
          {notifications.length === 0 ? (
            <p className={styles.empty}>알림이 없습니다.</p>
          ) : (
            <NotificationList notifications={notifications} onItemClick={onItemClick} />
          )}
        </div>
      </div>
    </div>
  );
}
