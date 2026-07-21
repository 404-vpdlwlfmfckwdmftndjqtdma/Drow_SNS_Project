// 결제 관련 백엔드 DTO와 1:1 대응하는 타입들.
// 모든 금액 단위는 원(KRW). 지갑 잔액도 같은 단위다.

/** GET /api/v1/wallet/me */
export interface WalletBalance {
  balance: number;
}

/** POST /api/v1/orders/charge → 이 orderId로 토스 결제창을 띄운다 */
export interface OrderCreateResponse {
  orderId: string;
  amount: number;
  purpose: "CHARGE";
}

/** POST /api/v1/orders/{orderId}/confirm */
export interface OrderConfirmResponse {
  orderId: string;
  purpose: "CHARGE";
  amount: number;
  balance: number;          // 충전 후 잔액
  alreadyProcessed: boolean; // 새로고침 등으로 중복 확정 요청한 경우 true
}

/** GET /api/v1/posts/{postId}/products */
export interface ProductOffer {
  capability: string;  // "textBlur" | "imageBlur" | ...
  price: number;
  purchased: boolean;
}

export interface ProductOfferResponse {
  postId: number;
  balance: number;
  offers: ProductOffer[];
}

/** POST /api/v1/posts/{postId}/purchase */
export interface PurchaseResponse {
  id: number;
  postId: number;
  capability: string;
  price: number;
  purchasedAt: string;
}

/** GET /api/v1/channels/{channelId}/tiers - 작가가 등록한 구독 상품 */
export interface TierResponse {
  id: number;
  name: string;
  monthlyPrice: number;
  description: string | null;
}
