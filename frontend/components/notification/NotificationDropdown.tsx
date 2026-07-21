"use client";

import { useEffect } from "react";
import NotificationList from "./NotificationList";
import type { AppNotification } from "@/types";
import styles from "./NotificationDropdown.module.css";

interface NotificationDropdownProps {
  notifications: AppNotification[];
  onItemClick: (notification: AppNotification) => void;
  onClose: () => void;
  selectMode: boolean;
  selectedIds: Set<number>;
  onToggleSelectMode: () => void;
  onToggleSelect: (id: number) => void;
  onDeleteSelected: () => void;
}

// [notification 모듈 - 드롭다운 콘텐츠] NotificationButton이 벨 아이콘 아래에 앵커로 띄우는 알림 목록.
// 화면 전체를 덮는 배경(오버레이) 없이, 바깥 영역 클릭은 NotificationButton이 감지해서 닫는다.
export default function NotificationDropdown({
  notifications,
  onItemClick,
  onClose,
  selectMode,
  selectedIds,
  onToggleSelectMode,
  onToggleSelect,
  onDeleteSelected,
}: NotificationDropdownProps) {
  useEffect(() => {
    function handleKeyDown(e: KeyboardEvent) {
      if (e.key === "Escape") onClose();
    }
    window.addEventListener("keydown", handleKeyDown);
    return () => window.removeEventListener("keydown", handleKeyDown);
  }, [onClose]);

  return (
    <div className={styles.dropdown}>
      <div className={styles.header}>
        <span className={styles.title}>알림</span>
        <div className={styles.headerActions}>
          {notifications.length > 0 && (
            <button type="button" className={styles.selectToggle} onClick={onToggleSelectMode}>
              {selectMode ? "취소" : "선택"}
            </button>
          )}
          <button className={styles.closeBtn} onClick={onClose} aria-label="닫기">
            <span className="material-symbols-outlined">close</span>
          </button>
        </div>
      </div>

      <div className={styles.body}>
        {notifications.length === 0 ? (
          <p className={styles.empty}>알림이 없습니다.</p>
        ) : (
          <NotificationList
            notifications={notifications}
            onItemClick={onItemClick}
            selectMode={selectMode}
            selectedIds={selectedIds}
            onToggleSelect={onToggleSelect}
          />
        )}
      </div>

      {selectMode && (
        <div className={styles.footer}>
          <span className={styles.selectedCount}>{selectedIds.size}개 선택됨</span>
          <button
            type="button"
            className={styles.deleteBtn}
            disabled={selectedIds.size === 0}
            onClick={onDeleteSelected}
          >
            삭제
          </button>
        </div>
      )}
    </div>
  );
}
