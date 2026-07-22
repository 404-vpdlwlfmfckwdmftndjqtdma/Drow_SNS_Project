"use client";

import { useState } from "react";
import { toErrorMessage } from "./errorMessage";
import { startCharge } from "./startCharge";
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
      // 결제창으로 리다이렉트된다 (정상 흐름에서는 이 아래가 실행되지 않는다)
      await startCharge(value, returnUrl);
    } catch (err) {
      // 결제창을 못 열거나 사용자가 닫은 경우
      setError(toErrorMessage(err, "충전을 시작하지 못했습니다."));
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
