"use client";

import type { BlurRange } from "@/types";
import styles from "./BlurredText.module.css";

interface BlurredTextProps {
  content: string;          // 서버가 내려준 본문 (비구독자면 이미 ● 로 치환된 상태)
  ranges: BlurRange[];      // 블러 구간 위치
  unlocked: boolean;        // 열람자가 구독자인가 (서버 판정 결과)
  onLockedClick?: () => void; // 잠긴 구간 클릭 시 (구독 유도 모달 등)
}

/**
 * [textblur 모듈 - 조회 화면용 렌더러]
 *
 * 무상태 컴포넌트: 서버가 준 완성본을 그리기만 한다.
 * - 비구독자: content 의 해당 구간이 이미 ●●● 이므로, 여기에 CSS 블러는 "장식"일 뿐이다.
 *   원문은 브라우저에 도착하지 않았으므로 개발자도구로도 볼 수 없다. (보안은 서버에서 종료)
 * - 구독자: 원문 구간을 강조 표시해 "구독으로 열린 부분"임을 보여준다.
 *
 * 원리: 본문을 ranges 의 start/end 로 조각내고, 조각마다 알맞은 스타일의 span 으로 렌더링.
 */
export default function BlurredText({ content, ranges, unlocked, onLockedClick }: BlurredTextProps) {
  const segments = splitByRanges(content, ranges);

  return (
    <span>
      {segments.map((seg, i) =>
        seg.blurred ? (
          <span
            key={i}
            className={unlocked ? styles.unlocked : styles.locked}
            title={unlocked ? "구독으로 열람 중인 구간" : "구독하면 볼 수 있어요"}
            onClick={unlocked ? undefined : onLockedClick}
          >
            {seg.text}
            {!unlocked && <span className={styles.lockIcon}>🔒</span>}
          </span>
        ) : (
          <span key={i}>{seg.text}</span>
        )
      )}
    </span>
  );
}

interface Segment {
  text: string;
  blurred: boolean;
}

/** 본문을 [일반, 블러, 일반, ...] 조각으로 분해 */
function splitByRanges(content: string, ranges: BlurRange[]): Segment[] {
  const sorted = [...ranges].sort((a, b) => a.start - b.start);
  const segments: Segment[] = [];
  let cursor = 0;

  for (const r of sorted) {
    const start = Math.max(0, Math.min(r.start, content.length));
    const end = Math.max(start, Math.min(r.end, content.length));
    if (cursor < start) {
      segments.push({ text: content.slice(cursor, start), blurred: false });
    }
    if (start < end) {
      segments.push({ text: content.slice(start, end), blurred: true });
    }
    cursor = Math.max(cursor, end);
  }
  if (cursor < content.length) {
    segments.push({ text: content.slice(cursor), blurred: false });
  }
  return segments;
}
