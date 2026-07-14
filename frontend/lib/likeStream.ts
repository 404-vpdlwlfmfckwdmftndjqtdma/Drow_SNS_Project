const API_BASE = process.env.NEXT_PUBLIC_API_URL;

// 좋아요 개수는 공개 정보라 인증 없이 구독한다 (백엔드 LikeController.subscribe 참고).
// 이 대상(targetType/targetId)을 보고 있는 모든 클라이언트에게 개수가 바뀔 때마다 브로드캐스트된다.
export function subscribeToLikeCount(
  targetType: "POST" | "COMMENT",
  targetId: number,
  onCountChange: (count: number) => void
): () => void {
  const source = new EventSource(`${API_BASE}/api/v1/likes/${targetType}/${targetId}/subscribe`);

  source.addEventListener("like-count", (event) => {
    onCountChange(Number((event as MessageEvent).data));
  });

  return () => source.close();
}
