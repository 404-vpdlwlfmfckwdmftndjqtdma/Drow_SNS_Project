"use client";

import { useState } from "react";
import { loadTossPayments } from "@tosspayments/tosspayments-sdk";
import api from "@/lib/api";
import type { ApiResponse } from "@/types";
import type { OrderCreateResponse } from "./types";
import { toErrorMessage } from "./errorMessage";
import {
  PAYMENT_FAIL_PATH,
  PAYMENT_RETURN_URL_KEY,
  PAYMENT_SUCCESS_PATH,
  TOSS_CLIENT_KEY,
} from "./config";
import styles from "./PaymentPanel.module.css";

/**
 * [충전 버튼] 지갑에 토큰을 충전한다. 외부 결제(토스)가 일어나는 유일한 지점.
 *
 * 흐름: ① 서버에 충전 주문 생성(POST /orders/charge) → 서버가 orderId·금액 확정
 *      ② 그 orderId로 토스 결제창 호출 (프론트가 orderId를 만들지 않는 것이 핵심)
 *      ③ 결제 후 success 페이지가 POST /orders/{orderId}/confirm 으로 승인 + 지갑 적립
 */
export default function ChargeButton({ returnUrl = "/payment" }: { returnUrl?: string }) {
  const [amount, setAmount] = useState("10000");
  const [pending, setPending] = useState(false);
  const [error, setError] = useState("");

  const handleCharge = async () => {
    const value = Number(amount);
    if (!Number.isInteger(value) || value <= 0) {
      setError("충전 금액은 1원 이상의 정수여야 합니다.");
      return;
    }

    setPending(true);
    setError("");
    try {
      // ① 주문 생성 - 금액은 서버가 저장하므로 이후 조작이 불가능하다
      const res = await api.post<ApiResponse<OrderCreateResponse>>("/api/v1/orders/charge", {
        amount: value,
      });
      const order = res.data.data;

      // 토스에서 돌아온 뒤 어느 화면으로 복귀할지 기억해 둔다
      sessionStorage.setItem(PAYMENT_RETURN_URL_KEY, returnUrl);

      // ② 서버가 발급한 orderId·amount 로 결제창 호출
      const toss = await loadTossPayments(TOSS_CLIENT_KEY);
      const payment = toss.payment({ customerKey: crypto.randomUUID() });
      await payment.requestPayment({
        method: "CARD",
        amount: { currency: "KRW", value: order.amount },
        orderId: order.orderId,
        orderName: "4NF 토큰 충전",
        successUrl: window.location.origin + PAYMENT_SUCCESS_PATH,
        failUrl: window.location.origin + PAYMENT_FAIL_PATH,
      });
    } catch (err) {
      // 결제창을 못 열거나 사용자가 닫은 경우 (정상 결제는 페이지가 넘어가므로 여기 안 옴)
      setError(toErrorMessage(err, "충전을 시작하지 못했습니다."));
      sessionStorage.removeItem(PAYMENT_RETURN_URL_KEY);
    } finally {
      setPending(false);
    }
  };

  return (
    <section className={styles.card}>
      <div className={styles.cardHeader}>
        <h2 className={styles.cardTitle}>충전</h2>
        <span className={styles.endpoint}>POST /api/v1/orders/charge</span>
      </div>
      <p className={styles.cardDesc}>토스 결제 후 지갑에 토큰이 적립됩니다.</p>

      <div className={styles.field}>
        <label className={styles.label} htmlFor="charge-amount">충전 금액(원)</label>
        <input
          id="charge-amount"
          className={styles.input}
          type="number"
          min={1}
          step={1000}
          value={amount}
          onChange={(e) => setAmount(e.target.value)}
        />
      </div>

      <button type="button" className={styles.button} onClick={handleCharge} disabled={pending}>
        {pending ? "결제창 여는 중..." : "충전하기"}
      </button>

      {error && <p className={styles.error}>{error}</p>}
    </section>
  );
}
