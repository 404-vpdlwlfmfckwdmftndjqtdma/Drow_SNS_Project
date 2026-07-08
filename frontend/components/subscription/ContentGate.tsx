import type { ContentVisibility } from "@/types";
import styles from "./ContentGate.module.css";

interface ContentGateProps {
  locked: boolean;
  visibility: ContentVisibility;
  children: React.ReactNode;
}

// 구독 여부(locked)와 공개범위(visibility)에 따라 콘텐츠를 다르게 렌더링.
// PUBLIC: 그대로 노출 / BLUR: 블러 오버레이 / BLACKBOX: 완전 비공개 문구 /
// RESTRICTED: 접근 제한 안내 / PARTIAL: 일부만 노출 + "더보기(구독)" CTA
export default function ContentGate({ locked, visibility, children }: ContentGateProps) {
  if (!locked) return <>{children}</>;

  // TODO: visibility 별 실제 마스킹 UI 구현
  return (
    <div className={styles.gate} data-visibility={visibility}>
      구독 후 확인할 수 있는 콘텐츠입니다.
    </div>
  );
}
