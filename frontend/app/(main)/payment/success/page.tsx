"use client";

import { Suspense, useEffect, useRef, useState } from "react";
import { useSearchParams } from "next/navigation";
import PaymentReturnButton from "@/components/modules/payment/PaymentReturnButton";
import styles from "@/components/modules/payment/PaymentStatus.module.css";
import {
  confirmPayment,
  type PaymentResultData,
} from "@/components/modules/payment/confirmPayment";
import PaymentResult from "@/components/modules/payment/PaymentResult";

function SuccessInner() {
  const params = useSearchParams();
  const [data, setData] = useState<PaymentResultData | null>(null);
  const [error, setError] = useState("");
  const done = useRef(false); // 중복 confirm 방지 (StrictMode 등)

  useEffect(() => {
    if (done.current) return;
    done.current = true;

    const paymentKey = params.get("paymentKey");
    const orderId = params.get("orderId");
    const amount = Number(params.get("amount"));

    if (!paymentKey || !orderId || !amount) {
      setError("잘못된 접근입니다. (결제 정보 없음)");
      return;
    }

    // 토스 리다이렉트로 받은 값 → 백엔드에 승인 요청
    confirmPayment({ paymentKey, orderId, amount })
      .then(setData)
      .catch(() => setError("결제 승인에 실패했습니다."));
  }, [params]);

  if (error) {
    return (
      <div className={styles.screen}>
        <section className={styles.card} aria-labelledby="payment-error-title">
          <div className={`${styles.statusIcon} ${styles.failureIcon}`} aria-hidden="true">
            <span className="material-symbols-outlined filled">priority_high</span>
          </div>
          <p className={`${styles.eyebrow} ${styles.failureEyebrow}`}>승인 확인 실패</p>
          <h1 className={styles.title} id="payment-error-title">결제 상태를 확인하지 못했어요</h1>
          <p className={styles.description}>결제가 승인되었을 수 있으니 다시 결제하기 전에 내역을 확인해 주세요.</p>
          <div className={styles.failureReason}>
            <span>안내</span>
            <strong>{error}</strong>
          </div>
          <div className={styles.actions}>
            <PaymentReturnButton />
          </div>
          <p className={styles.helpText}>문제가 계속되면 주문번호와 함께 고객센터에 문의해 주세요.</p>
        </section>
      </div>
    );
  }

  if (!data) {
    return (
      <div className={styles.screen} aria-live="polite">
        <section className={styles.card}>
          <div className={`${styles.statusIcon} ${styles.loadingIcon}`} aria-hidden="true">
            <span className={styles.spinner} />
          </div>
          <p className={styles.eyebrow}>결제 확인 중</p>
          <h1 className={styles.title}>결제 승인을 확인하고 있어요</h1>
          <p className={styles.description}>창을 닫거나 새로고침하지 말고 잠시만 기다려 주세요.</p>
        </section>
      </div>
    );
  }

  return (
    <div className={styles.screen}>
      <PaymentResult data={data} />
    </div>
  );
}

export default function SuccessPage() {
  // useSearchParams 는 Suspense 경계가 필요 (Next App Router)
  return (
    <Suspense
      fallback={(
        <div className={styles.screen}>
          <section className={styles.card}>결제 정보를 불러오고 있습니다.</section>
        </div>
      )}
    >
      <SuccessInner />
    </Suspense>
  );
}
