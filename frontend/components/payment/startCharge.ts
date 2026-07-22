import { loadTossPayments } from "@tosspayments/tosspayments-sdk";
import api from "@/lib/api";
import type { ApiResponse } from "@/types";
import type { OrderCreateResponse } from "./types";
import {
  PAYMENT_FAIL_PATH,
  PAYMENT_RETURN_URL_KEY,
  PAYMENT_SUCCESS_PATH,
  TOSS_CLIENT_KEY,
} from "./config";

/**
 * 토큰 충전 시작. 외부 결제(토스)가 일어나는 유일한 지점.
 *
 * ① 서버에 충전 주문 생성(금액은 서버가 저장 → 조작 불가)
 * ② 그 orderId로 토스 결제창 호출 → 결제창으로 리다이렉트된다(이 함수는 반환하지 않는다).
 * ③ 결제 후 success 페이지가 승인 + 지갑 적립을 마무리한다.
 *
 * @param amount   충전 금액(원)
 * @param returnUrl 결제 후 돌아올 앱 내부 경로
 */
export async function startCharge(amount: number, returnUrl: string): Promise<void> {
  const res = await api.post<ApiResponse<OrderCreateResponse>>("/api/v1/orders/charge", { amount });
  const order = res.data.data;

  sessionStorage.setItem(PAYMENT_RETURN_URL_KEY, returnUrl);

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
}
