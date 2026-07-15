"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import api from "@/lib/api";
import { isLoggedIn } from "@/lib/auth";
import type { ApiResponse } from "@/types";
import styles from "./FollowButton.module.css";

interface FollowButtonProps {
  targetUserId: number;
  // 팔로우/언팔로우가 실제로 성공했을 때만 호출된다(최초 상태 조회 시에는 호출 안 함) -
  // 부모 화면이 팔로워 수 같은 파생 값을 갱신할 때 쓴다.
  onFollowChange?: (following: boolean) => void;
}

// 팔로우/팔로잉 토글 버튼 (언팔로우 확인 모달 포함).
// 자체적으로 팔로우 여부를 조회/관리하므로, 마이페이지 상세뿐 아니라 채널(팔로잉) 목록 등
// 나중에 구조가 바뀌어도 어디서든 targetUserId만 넘기면 재사용할 수 있다.
export default function FollowButton({ targetUserId, onFollowChange }: FollowButtonProps) {
  const router = useRouter();
  const [following, setFollowing] = useState(false);
  const [busy, setBusy] = useState(false);
  const [showConfirm, setShowConfirm] = useState(false);

  useEffect(() => {
    if (!Number.isFinite(targetUserId)) return;

    (async () => {
      try {
        const res = await api.get<ApiResponse<boolean>>(`/api/v1/follows/${targetUserId}/status`);
        setFollowing(res.data.data);
      } catch {
        // 비로그인 등으로 실패하면 "팔로우 안 한 상태"로 둔다.
      }
    })();
  }, [targetUserId]);

  const applyChange = (value: boolean) => {
    setFollowing(value);
    onFollowChange?.(value);
  };

  const handleClick = async () => {
    if (!isLoggedIn()) {
      router.push("/login");
      return;
    }
    if (busy) return;

    if (following) {
      // 바로 언팔로우하지 않고, 자체 모달로 한 번 확인받는다 (브라우저 기본 confirm() 대신).
      setShowConfirm(true);
      return;
    }

    setBusy(true);
    try {
      await api.post(`/api/v1/follows/${targetUserId}`);
      applyChange(true);
    } catch {
      // 실패 시(이미 팔로우 중 등) 상태를 그대로 둔다.
    } finally {
      setBusy(false);
    }
  };

  const handleConfirmUnfollow = async () => {
    setShowConfirm(false);
    setBusy(true);
    try {
      await api.delete(`/api/v1/follows/${targetUserId}`);
      applyChange(false);
    } catch {
      // 실패 시 상태를 그대로 둔다.
    } finally {
      setBusy(false);
    }
  };

  return (
    <>
      <button
        className={following ? styles.glassBtn : styles.primaryBtn}
        type="button"
        onClick={handleClick}
        disabled={busy}
      >
        <span className="material-symbols-outlined filled" style={{ fontSize: 20 }}>
          {following ? "how_to_reg" : "person_add"}
        </span>
        {following ? "팔로잉" : "팔로우"}
      </button>

      {showConfirm && (
        <div className={styles.confirmOverlay} onClick={() => setShowConfirm(false)}>
          <div className={styles.confirmPanel} onClick={(e) => e.stopPropagation()}>
            <p className={styles.confirmText}>팔로우를 취소하시겠습니까?</p>
            <div className={styles.confirmActions}>
              <button className={styles.confirmCancelBtn} type="button" onClick={() => setShowConfirm(false)}>
                아니오
              </button>
              <button className={styles.confirmDeleteBtn} type="button" onClick={handleConfirmUnfollow}>
                예
              </button>
            </div>
          </div>
        </div>
      )}
    </>
  );
}
