"use client";

import { Suspense } from "react";
import { useSearchParams } from "next/navigation";
import PaymentReturnButton from "@/components/payment/PaymentReturnButton";
import styles from "@/components/payment/PaymentStatus.module.css";

function FailInner() {
  const params = useSearchParams();
  const message = params.get("message") ?? "결제가 취소되었거나 처리 중 문제가 발생했습니다.";
  const code = params.get("code");
  const orderId = params.get("orderId");

  return (
    <div className={styles.screen}>
      <section className={styles.card} aria-labelledby="payment-fail-title">
        <div className={`${styles.statusIcon} ${styles.failureIcon}`} aria-hidden="true">
          <span className="material-symbols-outlined filled">close</span>
        </div>

        <p className={`${styles.eyebrow} ${styles.failureEyebrow}`}>결제 미완료</p>
        <h1 className={styles.title} id="payment-fail-title">결제를 완료하지 못했어요</h1>
        <p className={styles.description}>결제 금액은 청구되지 않았습니다. 내용을 확인한 뒤 다시 시도해 주세요.</p>

        <div className={styles.failureReason}>
          <span>실패 사유</span>
          <strong>{message}</strong>
          {code && <p className={styles.errorCode}>오류 코드: {code}</p>}
        </div>

        {orderId && (
          <dl className={styles.details}>
            <div>
              <dt>주문번호</dt>
              <dd className={styles.orderId}>{orderId}</dd>
            </div>
          </dl>
        )}

        <div className={styles.actions}>
          <PaymentReturnButton />
        </div>

        <p className={styles.helpText}>같은 문제가 반복되면 다른 결제수단을 이용해 주세요.</p>
      </section>
    </div>
  );
}

export default function FailPage() {
  return (
    <Suspense
      fallback={(
        <div className={styles.screen}>
          <section className={styles.card}>결제 정보를 불러오고 있습니다.</section>
        </div>
      )}
    >
      <FailInner />
    </Suspense>
  );
}
