"use client";

import Link from "next/link";
import styles from "./CommentModal.module.css";

// 프로필 사진이 없을 때 닉네임 앞글자로 보여줄 배경색 팔레트. userId 기준으로 고정 배정된다.
const AVATAR_PALETTE = [
  "#F76C6C", "#4D96FF", "#6BCB77", "#FFC15F",
  "#9B5DE5", "#00BBF9", "#F15BB5", "#43AA8B",
];

function colorForId(id: number): string {
  return AVATAR_PALETTE[Math.abs(id) % AVATAR_PALETTE.length];
}

interface CommentAvatarProps {
  userId: number;
  nickname: string;
  profileImageUrl?: string | null;
  size?: number;
  linked?: boolean;
}

export default function CommentAvatar({ userId, nickname, profileImageUrl, size = 36, linked = true }: CommentAvatarProps) {
  const sizeStyle = { width: size, height: size, fontSize: Math.max(11, size * 0.36) };

  const content = profileImageUrl ? (
    <span
      className={styles.avatar}
      style={{ ...sizeStyle, backgroundImage: `url(${profileImageUrl})`, backgroundSize: "cover", backgroundPosition: "center" }}
    />
  ) : (
    <span className={styles.avatar} style={{ ...sizeStyle, background: colorForId(userId) }}>
      {nickname.slice(0, 2)}
    </span>
  );

  if (!linked) return content;

  return (
    <Link href={`/users/${userId}`} className={styles.avatarLink} onClick={(e) => e.stopPropagation()}>
      {content}
    </Link>
  );
}

// 삭제된 댓글은 실제 작성자 정보가 있어도 화면엔 익명으로 보여준다 (데이터는 그대로, 표시만 감춤).
export function UnknownAvatar({ size = 36 }: { size?: number }) {
  const sizeStyle = { width: size, height: size, fontSize: Math.max(11, size * 0.5) };
  return (
    <span className={`${styles.avatar} ${styles.avatarUnknown}`} style={sizeStyle}>
      ?
    </span>
  );
}
