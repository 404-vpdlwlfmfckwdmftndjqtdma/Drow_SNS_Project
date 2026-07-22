"use client";

import { useState } from "react";
import api from "@/lib/api";
import type { ApiResponse } from "@/types";
import type { TierResponse } from "./types";
import { toErrorMessage } from "./errorMessage";
import styles from "./PaymentPanel.module.css";

/**
 * [채널 구매 버튼] 작가를 구독해 그 작가 글의 블러를 전부 해제한다.
 *
 * 채널은 별도 개체가 아니라 작가(유저) 자신이므로 channelId = 작가의 userId 다.
 * 유료 구독은 등급(tier)이 있어야 하고 그 등급의 월 가격만큼 지갑에서 차감된다.
 */
export default function SubscribeButton({ onDone }: { onDone?: () => void }) {
  const [channelId, setChannelId] = useState("");
  const [tiers, setTiers] = useState<TierResponse[] | null>(null);
  const [tierId, setTierId] = useState("");
  const [pending, setPending] = useState(false);
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");

  // 등급 목록은 채널마다 다르므로 채널 ID를 넣고 조회해야 고를 수 있다
  const loadTiers = async () => {
    if (!channelId) {
      setError("채널 ID(작가 userId)를 입력하세요.");
      return;
    }
    setPending(true);
    setError("");
    setMessage("");
    try {
      const res = await api.get<TierResponse[]>(`/api/v1/channels/${channelId}/tiers`);
      setTiers(res.data);
      setTierId("");
      if (res.data.length === 0) {
        setMessage("등록된 구독 상품이 없습니다.");
      }
    } catch (err) {
      setError(toErrorMessage(err, "등급 목록을 불러오지 못했습니다."));
    } finally {
      setPending(false);
    }
  };

  const handleSubscribe = async () => {
    if (!channelId) {
      setError("채널 ID(작가 userId)를 입력하세요.");
      return;
    }
    if (!tierId) {
      setError("구독 상품을 선택하세요.");
      return;
    }
    setPending(true);
    setError("");
    setMessage("");
    try {
      await api.post<number>(`/api/v1/channels/${channelId}/subscriptions`, {
        tierId: Number(tierId),
      });
      const tier = tiers?.find((t) => String(t.id) === tierId);
      if (tier) {
        setMessage(`구독 완료 - ${tier.name}(월 ${tier.monthlyPrice.toLocaleString("ko-KR")}원) 차감됨`);
      }
      onDone?.();
    } catch (err) {
      setError(toErrorMessage(err, "구독에 실패했습니다."));
    } finally {
      setPending(false);
    }
  };

  return (
    <section className={styles.card}>
      <div className={styles.cardHeader}>
        <h2 className={styles.cardTitle}>채널 구매(구독)</h2>
        <span className={styles.endpoint}>POST /api/v1/channels/{"{id}"}/subscriptions</span>
      </div>
      <p className={styles.cardDesc}>구독하면 그 작가 글의 블러가 전부 풀립니다.</p>

      <div className={styles.field}>
        <label className={styles.label} htmlFor="sub-channel-id">채널 ID (작가 userId)</label>
        <div className={styles.row}>
          <input
            id="sub-channel-id"
            className={styles.input}
            type="number"
            min={1}
            placeholder="예: 14"
            value={channelId}
            onChange={(e) => setChannelId(e.target.value)}
          />
          <button
            type="button"
            className={styles.secondaryButton}
            onClick={loadTiers}
            disabled={pending}
          >
            등급 불러오기
          </button>
        </div>
      </div>

      <div className={styles.field}>
        <label className={styles.label} htmlFor="sub-tier">구독 등급</label>
        <select
          id="sub-tier"
          className={styles.select}
          value={tierId}
          onChange={(e) => setTierId(e.target.value)}
          disabled={tiers === null}
        >
          <option value="">구독 상품을 선택하세요</option>
          {tiers?.map((tier) => (
            <option key={tier.id} value={tier.id}>
              {tier.name} · 월 {tier.monthlyPrice.toLocaleString("ko-KR")}원
            </option>
          ))}
        </select>
      </div>

      <button
        type="button"
        className={styles.button}
        onClick={handleSubscribe}
        disabled={pending || !tierId}
      >
        {pending ? "처리 중..." : "구독하기"}
      </button>

      {message && <p className={styles.success}>{message}</p>}
      {error && <p className={styles.error}>{error}</p>}
    </section>
  );
}
