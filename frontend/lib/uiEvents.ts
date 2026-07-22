// 컴포넌트 간 느슨한 신호. 전역으로 흩어진 UI(상단 잔액 칩, 피드 목록)를
// 서로 직접 참조하지 않고 이벤트로 갱신하기 위한 얇은 유틸.

/** 지갑 잔액이 바뀌었다(충전·구매·구독). 상단 잔액 칩이 다시 조회한다. */
export const WALLET_CHANGED_EVENT = "4nf:wallet-changed";

/** 피드를 다시 불러와라(로고 클릭 등). 피드 목록이 재조회한다. */
export const FEED_REFRESH_EVENT = "4nf:feed-refresh";

function emit(name: string) {
  if (typeof window !== "undefined") {
    window.dispatchEvent(new Event(name));
  }
}

export function notifyWalletChanged() {
  emit(WALLET_CHANGED_EVENT);
}

export function requestFeedRefresh() {
  emit(FEED_REFRESH_EVENT);
}
