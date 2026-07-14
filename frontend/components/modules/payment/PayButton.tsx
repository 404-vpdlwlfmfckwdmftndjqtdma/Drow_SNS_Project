"use client";

import { ReactNode } from "react";
import { loadTossPayments } from "@tosspayments/tosspayments-sdk";
import {
  PAYMENT_FAIL_PATH,
  PAYMENT_RETURN_URL_KEY,
  PAYMENT_SUCCESS_PATH,
  TOSS_CLIENT_KEY,
} from "./config";

interface PayButtonProps {
  amount: number; // 결제 금액 (원)
  orderName: string; // 주문명
  returnUrl: string; // 결제 결과 확인 후 돌아갈 4nf 내부 경로
  successUrl?: string; // 성공 시 이동할 절대 URL (기본: /payment/success)
  failUrl?: string; // 실패 시 이동할 절대 URL (기본: /payment/fail)
  children?: ReactNode; // 버튼 라벨 (기본: "결제하기")
}

/**
 * [결제 버튼 모듈] 태그로 꽂아 쓰는 재사용 결제 버튼.
 * 클릭 → 토스 SDK requestPayment → 토스 결제창으로 "리다이렉트".
 * 결과는 이 컴포넌트가 아니라 successUrl/failUrl 라우트로 돌아온다.
 */
export default function PayButton({
  amount,
  orderName,
  returnUrl,
  successUrl,
  failUrl,
  children,
}: PayButtonProps) {
  const handlePay = async () => {
    try {
      // 토스 화면을 거쳐 돌아온 뒤에도 결제를 시작한 앱 경로를 알 수 있도록 저장한다.
      sessionStorage.setItem(
        PAYMENT_RETURN_URL_KEY,
        returnUrl,
      );

      const toss = await loadTossPayments(TOSS_CLIENT_KEY);
      const payment = toss.payment({ customerKey: crypto.randomUUID() }); // 테스트라 랜덤 고객키
      await payment.requestPayment({
        method: "CARD",
        amount: { currency: "KRW", value: amount },
        orderId: crypto.randomUUID(), // 주문번호 (토스가 successUrl 쿼리로 되돌려줌)
        orderName,
        successUrl: successUrl ?? window.location.origin + PAYMENT_SUCCESS_PATH,
        failUrl: failUrl ?? window.location.origin + PAYMENT_FAIL_PATH,
      });
    } catch (err) {
      // 결제창을 못 열거나 사용자가 창을 닫은 경우 (정상 결제는 페이지가 넘어가므로 여기 안 옴)
      console.error("결제 요청 실패:", err);
      sessionStorage.removeItem(PAYMENT_RETURN_URL_KEY);
      alert("결제를 시작하지 못했습니다.");
    }
  };

  return (
    <button type="button" onClick={handlePay}>
      {children ?? "결제하기"}
    </button>
  );
}
