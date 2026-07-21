"use client";

import { useCallback, useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import api from "@/lib/api";
import { isLoggedIn } from "@/lib/auth";
import { toErrorMessage } from "@/components/payment/errorMessage";
import type { TierResponse, WalletBalance } from "@/components/payment/types";
import type { ApiResponse, PageResponse } from "@/types";
import styles from "./SubscribeButton.module.css";

interface SubscribeButtonProps {
  /** 구독 대상 채널 = 작가의 userId (채널은 별도 개체가 아니라 작가 본인이다) */
  channelId: number;
  /** 구독/해지가 실제로 성공했을 때만 호출 (부모가 구독자 수 등 파생 값을 갱신할 때) */
  onSubscribeChange?: (subscribed: boolean) => void;
}

interface MySubscription {
  id: number;
  channelId: number;
  tierId: number | null;
  tierName: string | null;
  status: "ACTIVE" | "CANCELED";
}

/**
 * 채널(작가) 구독 토글 버튼. FollowButton과 같은 사용법 - channelId만 넘기면 된다.
 *
 * 구독하면 그 작가 글의 블러가 전부 풀린다. 결제는 지갑 차감이라 외부 결제창이 뜨지 않고,
 * 잔액이 모자라면 충전 화면으로 유도한다.
 */
export default function SubscribeButton({ channelId, onSubscribeChange }: SubscribeButtonProps) {
  const router = useRouter();
  const [subscribed, setSubscribed] = useState(false);
  const [tierName, setTierName] = useState<string | null>(null);
  const [busy, setBusy] = useState(false);

  // 등급 선택 모달 상태
  const [pickerOpen, setPickerOpen] = useState(false);
  const [tiers, setTiers] = useState<TierResponse[]>([]);
  const [balance, setBalance] = useState(0);
  const [error, setError] = useState("");

  // 해지 확인 모달
  const [showConfirm, setShowConfirm] = useState(false);

  // 구독 여부 조회.
  // 팔로우와 달리 단건 상태 API(/follows/{id}/status 같은 것)가 없어서 내 구독 목록에서 찾는다.
  const loadStatus = useCallback(async () => {
    if (!Number.isFinite(channelId) || !isLoggedIn()) return;
    try {
      const res = await api.get<ApiResponse<PageResponse<MySubscription>>>("/api/v1/subscriptions/me");
      const mine = res.data.data.content.find(
        (s) => s.channelId === channelId && s.status === "ACTIVE"
      );
      setSubscribed(Boolean(mine));
      setTierName(mine?.tierName ?? null);
    } catch {
      // 비로그인 등으로 실패하면 "구독 안 한 상태"로 둔다.
    }
  }, [channelId]);

  useEffect(() => {
    loadStatus();
  }, [loadStatus]);

  const applyChange = (value: boolean) => {
    setSubscribed(value);
    onSubscribeChange?.(value);
  };

  // 구독 버튼 클릭 → 등급과 잔액을 함께 불러와 모달을 연다
  const openPicker = async () => {
    if (!isLoggedIn()) {
      router.push("/login");
      return;
    }
    setBusy(true);
    setError("");
    try {
      const [tierRes, walletRes] = await Promise.all([
        api.get<TierResponse[]>(`/api/v1/channels/${channelId}/tiers`),
        api.get<ApiResponse<WalletBalance>>("/api/v1/wallet/me"),
      ]);
      setTiers(tierRes.data);
      setBalance(walletRes.data.data.balance);
      setPickerOpen(true);
    } catch (err) {
      setError(toErrorMessage(err, "구독 정보를 불러오지 못했습니다."));
    } finally {
      setBusy(false);
    }
  };

  // tierId가 null이면 무료 구독(차감 없음, 블러 해제 혜택도 없음)
  const handleSubscribe = async (tierId: number | null, name: string | null) => {
    setBusy(true);
    setError("");
    try {
      await api.post(`/api/v1/channels/${channelId}/subscriptions`, { tierId });
      setTierName(name);
      applyChange(true);
      setPickerOpen(false);
    } catch (err) {
      setError(toErrorMessage(err, "구독에 실패했습니다."));
    } finally {
      setBusy(false);
    }
  };

  const handleUnsubscribe = async () => {
    setShowConfirm(false);
    setBusy(true);
    try {
      await api.delete(`/api/v1/channels/${channelId}/subscriptions`);
      setTierName(null);
      applyChange(false);
    } catch (err) {
      setError(toErrorMessage(err, "구독 해지에 실패했습니다."));
    } finally {
      setBusy(false);
    }
  };

  return (
    <>
      <button
        className={subscribed ? styles.glassBtn : styles.primaryBtn}
        type="button"
        onClick={() => (subscribed ? setShowConfirm(true) : openPicker())}
        disabled={busy}
      >
        <span className="material-symbols-outlined filled" style={{ fontSize: 20 }}>
          {subscribed ? "workspace_premium" : "add_circle"}
        </span>
        {subscribed ? tierName ?? "구독중" : "구독"}
      </button>

      {error && !pickerOpen && <p className={styles.error}>{error}</p>}

      {/* 등급 선택 모달 - 보유 토큰과 각 등급 가격을 비교해 부족하면 충전으로 보낸다 */}
      {pickerOpen && (
        <div className={styles.confirmOverlay} onClick={() => setPickerOpen(false)}>
          <div className={styles.pickerPanel} onClick={(e) => e.stopPropagation()}>
            <p className={styles.pickerTitle}>구독 등급 선택</p>
            <p className={styles.balance}>보유 토큰 {balance.toLocaleString("ko-KR")}원</p>

            <ul className={styles.tierList}>
              {tiers.map((tier) => {
                const short = tier.monthlyPrice > balance;
                return (
                  <li key={tier.id}>
                    <button
                      className={styles.tierItem}
                      type="button"
                      disabled={busy || short}
                      onClick={() => handleSubscribe(tier.id, tier.name)}
                    >
                      <span className={styles.tierName}>
                        {tier.name}
                      </span>
                      <span className={styles.tierPrice}>
                        월 {tier.monthlyPrice.toLocaleString("ko-KR")}원
                        {short && " · 잔액 부족"}
                      </span>
                    </button>
                  </li>
                );
              })}

              <li>
                <button
                  className={styles.tierItem}
                  type="button"
                  disabled={busy}
                  onClick={() => handleSubscribe(null, null)}
                >
                  <span className={styles.tierName}>무료 구독</span>
                  <span className={styles.tierPrice}>차감 없음 · 블러 해제 안 됨</span>
                </button>
              </li>
            </ul>

            {error && <p className={styles.error}>{error}</p>}

            <div className={styles.confirmActions}>
              <button
                className={styles.confirmCancelBtn}
                type="button"
                onClick={() => router.push("/payment")}
              >
                충전하러 가기
              </button>
              <button
                className={styles.confirmCancelBtn}
                type="button"
                onClick={() => setPickerOpen(false)}
              >
                닫기
              </button>
            </div>
          </div>
        </div>
      )}

      {/* 해지 확인 - FollowButton과 동일한 패턴 */}
      {showConfirm && (
        <div className={styles.confirmOverlay} onClick={() => setShowConfirm(false)}>
          <div className={styles.confirmPanel} onClick={(e) => e.stopPropagation()}>
            <p className={styles.confirmText}>구독을 해지하시겠습니까?</p>
            <div className={styles.confirmActions}>
              <button className={styles.confirmCancelBtn} type="button" onClick={() => setShowConfirm(false)}>
                아니오
              </button>
              <button className={styles.confirmDeleteBtn} type="button" onClick={handleUnsubscribe}>
                예
              </button>
            </div>
          </div>
        </div>
      )}
    </>
  );
}
