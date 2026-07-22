"use client";

import { useCallback, useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import api from "@/lib/api";
import { isLoggedIn } from "@/lib/auth";
import { toErrorMessage } from "@/components/payment/errorMessage";
import { savePendingAction } from "@/components/payment/pendingAction";
import { startCharge } from "@/components/payment/startCharge";
import { notifyWalletChanged } from "@/lib/uiEvents";
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
  tierId: number;
  tierName: string;
  status: "ACTIVE" | "CANCELED";
}

/**
 * 채널(작가) 구독 토글 버튼. FollowButton과 같은 사용법 - channelId만 넘기면 된다.
 *
 * 구독하면 그 작가 글의 블러가 전부 풀린다. 결제는 지갑 차감이다.
 * 잔액이 모자라면 "부족분(상품가 - 잔액)"만 충전하도록 안내하고, 충전이 끝나면
 * 원래 하려던 구독이 자동으로 완료된다(success 페이지가 pendingAction을 실행).
 */
export default function SubscribeButton({ channelId, onSubscribeChange }: SubscribeButtonProps) {
  const router = useRouter();
  const [subscribed, setSubscribed] = useState(false);
  const [tierName, setTierName] = useState<string | null>(null);
  const [busy, setBusy] = useState(false);

  // 등급 선택 모달
  const [pickerOpen, setPickerOpen] = useState(false);
  const [tiers, setTiers] = useState<TierResponse[]>([]);
  const [balance, setBalance] = useState(0);
  const [error, setError] = useState("");

  // 잔액이 모자란 등급을 고르면 이 값이 채워지고 "부족분 결제" 화면으로 바뀐다
  const [payingTier, setPayingTier] = useState<TierResponse | null>(null);

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
    setPayingTier(null);
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

  // 등급 클릭: 잔액이 넉넉하면 바로 구독, 모자라면 부족분 결제 화면으로
  const pickTier = (tier: TierResponse) => {
    if (tier.monthlyPrice > balance) {
      setPayingTier(tier);
    } else {
      subscribe(tier.id, tier.name);
    }
  };

  const subscribe = async (tierId: number, name: string) => {
    setBusy(true);
    setError("");
    try {
      await api.post(`/api/v1/channels/${channelId}/subscriptions`, { tierId });
      setTierName(name);
      applyChange(true);
      setPickerOpen(false);
      notifyWalletChanged();   // 유료 구독이면 잔액이 줄었으니 상단 칩 갱신
    } catch (err) {
      setError(toErrorMessage(err, "구독에 실패했습니다."));
    } finally {
      setBusy(false);
    }
  };

  // 부족분만 충전 → 토스로 이동. 돌아오면 success 페이지가 이 구독을 자동 완료한다.
  const chargeAndSubscribe = async (tier: TierResponse) => {
    setBusy(true);
    setError("");
    try {
      savePendingAction({
        type: "subscribe",
        channelId,
        tierId: tier.id,
        label: `${tier.name} 구독`,
      });
      const shortfall = tier.monthlyPrice - balance;
      await startCharge(shortfall, window.location.pathname + window.location.search);
    } catch (err) {
      setError(toErrorMessage(err, "충전을 시작하지 못했습니다."));
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

  const closePicker = () => {
    setPickerOpen(false);
    setPayingTier(null);
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

      {pickerOpen && (
        <div className={styles.confirmOverlay} onClick={closePicker}>
          <div className={styles.pickerPanel} onClick={(e) => e.stopPropagation()}>
            {payingTier ? (
              /* ── 부족분 결제 화면 ── */
              <>
                <p className={styles.pickerTitle}>{payingTier.name} 구독</p>
                <dl className={styles.breakdown}>
                  <div>
                    <dt>상품 금액</dt>
                    <dd>{payingTier.monthlyPrice.toLocaleString("ko-KR")}원</dd>
                  </div>
                  <div>
                    <dt>보유 토큰</dt>
                    <dd>- {balance.toLocaleString("ko-KR")}원</dd>
                  </div>
                  <div className={styles.breakdownTotal}>
                    <dt>결제할 금액</dt>
                    <dd>{(payingTier.monthlyPrice - balance).toLocaleString("ko-KR")}원</dd>
                  </div>
                </dl>

                {error && <p className={styles.error}>{error}</p>}

                <button
                  className={styles.payButton}
                  type="button"
                  disabled={busy}
                  onClick={() => chargeAndSubscribe(payingTier)}
                >
                  {(payingTier.monthlyPrice - balance).toLocaleString("ko-KR")}원 결제하고 구독하기
                </button>
                <button className={styles.confirmCancelBtn} type="button" onClick={() => setPayingTier(null)}>
                  뒤로
                </button>
              </>
            ) : (
              /* ── 등급 선택 화면 ── */
              <>
                <p className={styles.pickerTitle}>구독 상품 선택</p>
                <p className={styles.balance}>보유 토큰 {balance.toLocaleString("ko-KR")}원</p>

                {tiers.length === 0 && (
                  <p className={styles.error}>등록된 구독 상품이 없습니다.</p>
                )}

                <ul className={styles.tierList}>
                  {tiers.map((tier) => {
                    const short = tier.monthlyPrice > balance;
                    return (
                      <li key={tier.id}>
                        <button
                          className={styles.tierItem}
                          type="button"
                          disabled={busy}
                          onClick={() => pickTier(tier)}
                        >
                          <span className={styles.tierName}>{tier.name}</span>
                          <span className={styles.tierPrice}>
                            월 {tier.monthlyPrice.toLocaleString("ko-KR")}원
                            {short && " · 충전 필요"}
                          </span>
                        </button>
                      </li>
                    );
                  })}
                </ul>

                {error && <p className={styles.error}>{error}</p>}

                <button className={styles.confirmCancelBtn} type="button" onClick={closePicker}>
                  닫기
                </button>
              </>
            )}
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
