"use client";

import { useState } from "react";
import api from "@/lib/api";
import type { ApiResponse } from "@/types";
import type { ProductOfferResponse, PurchaseResponse } from "./types";
import { toErrorMessage } from "./errorMessage";
import styles from "./PaymentPanel.module.css";

interface PurchaseButtonProps {
  capability: string;   // 구매할 기능 key ("textBlur" | "imageBlur")
  title: string;        // 카드 제목
  description?: string;
  onDone?: () => void;  // 구매 성공 시 부모에게 알림 (잔액 갱신용)
}

/**
 * [개별 상품 구매 버튼] 특정 글의 특정 기능 잠금을 지갑 잔액으로 산다.
 *
 * 가격은 판매자가 글에 등록해 둔 가격표에서 서버가 조회하므로 프론트가 보내지 않는다.
 * "가격 확인"으로 먼저 조회하면 이 글에서 이 기능이 얼마인지 / 이미 샀는지 알 수 있다.
 */
export default function PurchaseButton({
  capability,
  title,
  description,
  onDone,
}: PurchaseButtonProps) {
  const [postId, setPostId] = useState("");
  const [pending, setPending] = useState(false);
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");

  // 이 글에서 파는 상품 목록을 조회해 해당 기능의 가격/구매여부만 뽑아 보여준다
  const checkPrice = async () => {
    if (!postId) {
      setError("게시글 ID를 입력하세요.");
      return;
    }
    setPending(true);
    setError("");
    setMessage("");
    try {
      const res = await api.get<ApiResponse<ProductOfferResponse>>(
        `/api/v1/posts/${postId}/products`
      );
      const data = res.data.data;
      const offer = data.offers.find((o) => o.capability === capability);
      if (!offer) {
        setMessage(`이 글은 ${capability}를 판매하지 않습니다. (보유 ${data.balance.toLocaleString("ko-KR")}원)`);
        return;
      }
      setMessage(
        `가격 ${offer.price.toLocaleString("ko-KR")}원 · 보유 ${data.balance.toLocaleString("ko-KR")}원` +
          (offer.purchased ? " · 이미 구매함" : offer.price > data.balance ? " · 잔액 부족" : "")
      );
    } catch (err) {
      setError(toErrorMessage(err, "가격을 불러오지 못했습니다."));
    } finally {
      setPending(false);
    }
  };

  const handlePurchase = async () => {
    if (!postId) {
      setError("게시글 ID를 입력하세요.");
      return;
    }
    setPending(true);
    setError("");
    setMessage("");
    try {
      const res = await api.post<ApiResponse<PurchaseResponse>>(
        `/api/v1/posts/${postId}/purchase`,
        { capability }
      );
      const purchase = res.data.data;
      setMessage(`구매 완료 - ${purchase.price.toLocaleString("ko-KR")}원 차감됨 (잠금 해제)`);
      onDone?.();
    } catch (err) {
      setError(toErrorMessage(err, "구매에 실패했습니다."));
    } finally {
      setPending(false);
    }
  };

  return (
    <section className={styles.card}>
      <div className={styles.cardHeader}>
        <h2 className={styles.cardTitle}>{title}</h2>
        <span className={styles.endpoint}>POST /api/v1/posts/{"{id}"}/purchase</span>
      </div>
      {description && <p className={styles.cardDesc}>{description}</p>}

      <div className={styles.field}>
        <label className={styles.label} htmlFor={`purchase-post-${capability}`}>
          게시글 ID
        </label>
        <input
          id={`purchase-post-${capability}`}
          className={styles.input}
          type="number"
          min={1}
          placeholder="예: 27"
          value={postId}
          onChange={(e) => setPostId(e.target.value)}
        />
      </div>

      <div className={styles.row}>
        <button
          type="button"
          className={styles.secondaryButton}
          onClick={checkPrice}
          disabled={pending}
        >
          가격 확인
        </button>
        <button type="button" className={styles.button} onClick={handlePurchase} disabled={pending}>
          {pending ? "처리 중..." : "구매하기"}
        </button>
      </div>

      {message && <p className={styles.success}>{message}</p>}
      {error && <p className={styles.error}>{error}</p>}
    </section>
  );
}
