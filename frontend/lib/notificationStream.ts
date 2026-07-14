import type { AppNotification } from "@/types";

const API_BASE = process.env.NEXT_PUBLIC_API_URL;

// 브라우저 EventSource는 커스텀 헤더(X-User-Id)를 못 보내므로 쿼리 파라미터로 사용자를 식별한다.
// (백엔드 NotificationController.subscribe 참고 - JWT 붙으면 다 같이 토큰 기반으로 전환 예정)
// 연결이 끊기면 EventSource가 알아서 재연결을 시도한다 (기본 재시도 간격 ~3초).
export function subscribeToNotifications(
  userId: number,
  onNotification: (notification: AppNotification) => void,
  onConnectionChange?: (connected: boolean) => void
): () => void {
  const source = new EventSource(`${API_BASE}/api/v1/notifications/subscribe?userId=${userId}`);

  source.addEventListener("notification", (event) => {
    onNotification(JSON.parse((event as MessageEvent).data));
  });
  source.onopen = () => onConnectionChange?.(true);
  // EventSource가 재연결을 시도하는 동안에도 error가 발생하므로, 연결이 끊긴 동안엔 계속 false로 보여준다.
  source.onerror = () => onConnectionChange?.(false);

  return () => source.close();
}
