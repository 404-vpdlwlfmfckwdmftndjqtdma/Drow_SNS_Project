/**
 * [결제 후 이어서 할 일]
 *
 * 잔액이 모자라 충전부터 해야 할 때, 원래 하려던 구매를 여기 적어두고 토스로 떠난다.
 * 충전이 끝나면 success 페이지가 이걸 읽어 자동으로 마저 처리하므로,
 * 사용자는 "충전하고 다시 구매 버튼 누르기"를 하지 않아도 된다.
 *
 * 토스 결제는 리다이렉트라 페이지가 통째로 바뀐다. 그래서 메모리가 아니라
 * sessionStorage에 남긴다(탭을 닫으면 사라진다).
 */

const KEY = "4nf_pending_action";

export type PendingAction =
  | { type: "purchase"; postId: number; capability: string; label: string }
  | { type: "subscribe"; channelId: number; tierId: number | null; label: string };

export function savePendingAction(action: PendingAction) {
  sessionStorage.setItem(KEY, JSON.stringify(action));
}

/** 한 번 읽으면 지운다 - 새로고침으로 같은 구매가 두 번 일어나면 안 된다. */
export function takePendingAction(): PendingAction | null {
  const raw = sessionStorage.getItem(KEY);
  sessionStorage.removeItem(KEY);
  if (!raw) return null;
  try {
    return JSON.parse(raw) as PendingAction;
  } catch {
    return null;
  }
}

export function clearPendingAction() {
  sessionStorage.removeItem(KEY);
}
