"use client";

import { useCallback, useEffect, useState } from "react";
import api from "@/lib/api";
import type { ApiResponse } from "@/types";
import type { ProductOffer, ProductOfferResponse, PurchaseResponse } from "./types";
import { toErrorMessage } from "./errorMessage";
import { savePendingAction } from "./pendingAction";
import { startCharge } from "./startCharge";
import { notifyWalletChanged } from "@/lib/uiEvents";
import styles from "./PaymentPanel.module.css";

interface PurchaseButtonProps {
  postId: number;
  capability: string;   // "textBlur" | "imageBlur" - 감싸는 쪽에서 못 박는다
  label: string;        // 버튼/안내에 쓸 이름 (예: "텍스트 블러")
  onDone?: () => void;  // 구매 성공 시 부모 갱신 (블러 풀린 화면 다시 그리기). 없어도 동작한다.
}

/**
 * [블러 구매 버튼 - 공용 구현] postId만 있으면 알아서 굴러간다.
 *
 * 마운트 시 이 글에서 이 기능이 얼마인지·이미 샀는지·파는지 조회한다.
 *  - 판매 안 하는 글 → 아무것도 안 그린다.
 *  - 이미 구매 → "구매함" 표시.
 *  - 살 수 있음 → "○○ 구매 (가격)" 버튼. 잔액이 모자라면 부족분(가격-잔액)을
 *    자동 충전한 뒤 돌아와서 구매까지 이어진다(success 페이지가 마무리).
 *
 * 캡ability는 감싸는 컴포넌트(TextBlur/ImageBlurPurchaseButton)가 고정하므로
 * 쓰는 쪽은 postId만 넘기면 된다.
 */
export default function PurchaseButton({ postId, capability, label, onDone }: PurchaseButtonProps) {
  const [offer, setOffer] = useState<ProductOffer | null>(null);
  const [balance, setBalance] = useState(0);
  const [state, setState] = useState<"loading" | "ready" | "unavailable">("loading");
  const [pending, setPending] = useState(false);
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");

  // 이 글에서 이 기능의 가격/구매여부/잔액을 조회한다
  const load = useCallback(async () => {
    if (!Number.isInteger(postId) || postId <= 0) {
      setState("unavailable");
      return;
    }
    setState("loading");
    setMessage("");
    setError("");
    try {
      const res = await api.get<ApiResponse<ProductOfferResponse>>(`/api/v1/posts/${postId}/products`);
      const found = res.data.data.offers.find((o) => o.capability === capability) ?? null;
      setOffer(found);
      setBalance(res.data.data.balance);
      setState(found ? "ready" : "unavailable");
    } catch {
      setState("unavailable");
    }
  }, [postId, capability]);

  useEffect(() => {
    load();
  }, [load]);

  const handleClick = async () => {
    if (!offer) return;
    setPending(true);
    setMessage("");
    setError("");
    try {
      // 잔액이 모자라면 부족분만 충전 → 돌아오면 success 페이지가 이 구매를 자동 완료한다
      if (offer.price > balance) {
        const shortfall = offer.price - balance;
        savePendingAction({ type: "purchase", postId, capability, label: `${label} 구매` });
        await startCharge(shortfall, window.location.pathname + window.location.search);
        return; // 결제창으로 리다이렉트됨
      }

      const res = await api.post<ApiResponse<PurchaseResponse>>(
        `/api/v1/posts/${postId}/purchase`,
        { capability }
      );
      setMessage(`${res.data.data.price.toLocaleString("ko-KR")}원 결제 완료 · 잠금 해제`);
      notifyWalletChanged();
      onDone?.();
      await load(); // "구매함" 상태로 갱신
    } catch (err) {
      setError(toErrorMessage(err, "구매에 실패했습니다."));
    } finally {
      setPending(false);
    }
  };

  if (state === "loading") {
    return <p className={styles.info}>{label} 확인 중...</p>;
  }
  // 판매하지 않는 글이면 버튼 자체를 그리지 않는다 (게시글 상세에 붙여도 깔끔)
  if (state === "unavailable" || !offer) {
    return null;
  }
  if (offer.purchased) {
    return (
      <span className={styles.ownedTag}>
        <span className="material-symbols-outlined" style={{ fontSize: 18 }}>lock_open</span>
        {label} 구매함
      </span>
    );
  }

  const short = offer.price > balance;
  const shortfall = offer.price - balance;

  return (
    <div className={styles.field}>
      {/* 상품가 / 보유토큰 / 결제할 금액을 위아래로 */}
      <dl className={styles.breakdown}>
        <div>
          <dt>{label} 가격</dt>
          <dd>{offer.price.toLocaleString("ko-KR")}원</dd>
        </div>
        <div>
          <dt>보유 토큰</dt>
          <dd>{balance.toLocaleString("ko-KR")}원</dd>
        </div>
        <div className={styles.breakdownTotal}>
          <dt>{short ? "충전 후 결제" : "결제 금액"}</dt>
          <dd>{(short ? shortfall : offer.price).toLocaleString("ko-KR")}원</dd>
        </div>
      </dl>

      <button type="button" className={styles.buyButton} onClick={handleClick} disabled={pending}>
        {pending
          ? "처리 중..."
          : short
            ? `${shortfall.toLocaleString("ko-KR")}원 충전하고 구매하기`
            : `${offer.price.toLocaleString("ko-KR")}원 결제하고 구매하기`}
      </button>

      {message && <p className={styles.success}>{message}</p>}
      {error && <p className={styles.error}>{error}</p>}
    </div>
  );
}
