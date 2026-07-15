import Link from "next/link";
import styles from "./CompactChannelList.module.css";

export interface CompactChannelItem {
  id: number;
  name: string;
  description?: string;
  profileImageUrl?: string;
}

interface CompactChannelListProps {
  channels?: CompactChannelItem[];
}

const PREVIEW_CHANNELS: CompactChannelItem[] = [
  {
    id: 1,
    name: "Elena Rivers",
    description: "생성 예술과 인간 감정의 교차점을 탐구합니다.",
  },
];

// 채널 도메인의 페이지 구조와 분리된 우측 패널용 목록 UI.
// 채널 목록 API가 완성되면 channels prop에 조회 결과만 전달하면 된다.
export default function CompactChannelList({ channels = PREVIEW_CHANNELS }: CompactChannelListProps) {
  return (
    <section className={styles.panel} aria-labelledby="compact-channel-title">
      <div className={styles.header}>
        <div>
          <p className={styles.eyebrow}>DISCOVER</p>
          <h2 id="compact-channel-title" className={styles.title}>채널</h2>
        </div>
        <Link href="/channels" className={styles.moreLink} aria-label="채널 전체 보기">
          전체 보기
          <span className="material-symbols-outlined">arrow_forward</span>
        </Link>
      </div>

      <div className={styles.list}>
        {channels.map((channel) => (
          <Link key={channel.id} href={`/users/${channel.id}`} className={styles.channelItem}>
            <span
              className={styles.avatar}
              style={channel.profileImageUrl ? { backgroundImage: `url(${channel.profileImageUrl})` } : undefined}
              aria-hidden="true"
            >
              {!channel.profileImageUrl && channel.name.slice(0, 1).toUpperCase()}
            </span>
            <span className={styles.copy}>
              <strong className={styles.name}>{channel.name}</strong>
              <span className={styles.description}>{channel.description || "채널 소개가 없습니다."}</span>
            </span>
            <span className={`material-symbols-outlined ${styles.chevron}`}>chevron_right</span>
          </Link>
        ))}
      </div>
    </section>
  );
}
