"use client";

import { Suspense, useEffect, useRef, useState } from "react";
import { useSearchParams } from "next/navigation";
import api from "@/lib/api";
import ChargeResult from "@/components/payment/ChargeResult";
import PaymentReturnButton from "@/components/payment/PaymentReturnButton";
import { toErrorMessage } from "@/components/payment/errorMessage";
import { runPendingAction, takePendingAction } from "@/components/payment/pendingAction";
import { notifyWalletChanged } from "@/lib/uiEvents";
import type { OrderConfirmResponse } from "@/components/payment/types";
import styles from "@/components/payment/PaymentStatus.module.css";
import type { ApiResponse } from "@/types";

/**
 * 토스 결제 후 착지 페이지. 여기서 승인 + 지갑 적립이 확정된다.
 *
 * orderId는 프론트가 만든 값이 아니라 서버가 발급한 주문번호이고,
 * 금액도 서버가 주문에 저장해 둔 값을 쓰므로 여기서는 paymentKey만 보낸다.
 */
function SuccessInner() {
  const params = useSearchParams();
  const [data, setData] = useState<OrderConfirmResponse | null>(null);
  const [error, setError] = useState("");
  // 충전이 "부족분 결제 → 구매 완료" 흐름의 일부였다면 그 결과 메시지
  const [followUp, setFollowUp] = useState<string | null>(null);
  const done = useRef(false); // 중복 confirm 방지 (StrictMode 등)

  useEffect(() => {
    if (done.current) return;
    done.current = true;

    const paymentKey = params.get("paymentKey");
    const orderId = params.get("orderId");

    if (!paymentKey || !orderId) {
      setError("잘못된 접근입니다. (결제 정보 없음)");
      return;
    }

    (async () => {
      try {
        // ① 충전 승인 + 지갑 적립
        const res = await api.post<ApiResponse<OrderConfirmResponse>>(
          `/api/v1/orders/${orderId}/confirm`,
          { paymentKey }
        );
        setData(res.data.data);
        notifyWalletChanged();   // 충전으로 잔액 증가 → 상단 칩 갱신

        // ② 부족분 충전이었다면, 원래 하려던 구매를 이어서 완료한다
        const pending = takePendingAction();
        if (pending) {
          try {
            await runPendingAction(pending);
            setFollowUp(`${pending.label} 완료`);
          } catch (err) {
            // 충전은 됐지만 구매가 실패한 경우(이미 구독 중 등) - 잔액은 지갑에 남는다
            setFollowUp(toErrorMessage(err, `${pending.label}은(는) 완료하지 못했습니다.`));
          }
        }
      } catch (err) {
        setError(toErrorMessage(err, "결제 승인에 실패했습니다."));
      }
    })();
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
      <ChargeResult data={data} followUp={followUp} />
    </div>
  );
}

export default function SuccessPage() {
  // useSearchParams 는 Suspense 경계가 필요 (Next App Router)
  return (
    <Suspense>
      <SuccessInner />
    </Suspense>
  );
}
