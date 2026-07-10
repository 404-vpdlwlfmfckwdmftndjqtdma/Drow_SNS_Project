import api from "@/lib/api";

// success 페이지가 토스 리다이렉트 쿼리에서 받은 값
export interface ConfirmParams {
  paymentKey: string;
  orderId: string;
  amount: number;
}

// 백엔드가 토스 승인 후 돌려주는 결제 결과
export interface PaymentResultData {
  paymentKey: string;
  orderId: string;
  orderName: string;
  amount: number;
  status: string;
  method: string;
  approvedAt: string;
}

/**
 * success 페이지에서 호출 → 백엔드 /confirm 로 승인 요청.
 * 백엔드가 secretKey로 토스에 실제 승인하고 결과를 저장한 뒤 반환한다.
 */
export async function confirmPayment(params: ConfirmParams): Promise<PaymentResultData> {
  const res = await api.post<PaymentResultData>("/api/v1/payments/confirm", params);
  return res.data;
}
