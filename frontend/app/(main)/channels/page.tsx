"use client";

import { useEffect, useState } from "react";
import Link from "next/link";
import api from "@/lib/api";
import FollowButton from "@/components/follow/FollowButton";
import type { ApiResponse } from "@/types";
import styles from "./page.module.css";

// FollowUserResponse 와 1:1로 맞춘 응답 타입.
interface FollowingUser {
  userId: number;
  nickname: string;
  profileImageUrl?: string;
  bio?: string;
}

// 채널 = 내가 팔로우한 사람 목록 (마이페이지 CompactChannelList의 "전체 보기" 목적지).
// GET /api/v1/follows/following 으로 연동 (본인 팔로잉 목록, 로그인 필요).
// 항목의 팔로우 버튼은 FollowButton을 그대로 재사용 - 언팔로우하면 콜백으로 목록에서 바로 제거한다.
export default function ChannelListPage() {
  const [channels, setChannels] = useState<FollowingUser[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    (async () => {
      try {
        const res = await api.get<ApiResponse<FollowingUser[]>>("/api/v1/follows/following");
        setChannels(res.data.data);
      } catch {
        // 비로그인 등으로 실패하면 빈 목록으로 둔다.
      } finally {
        setLoading(false);
      }
    })();
  }, []);

  const handleFollowChange = (userId: number, following: boolean) => {
    if (!following) {
      setChannels((prev) => prev.filter((channel) => channel.userId !== userId));
    }
  };

  return (
    <main className={styles.container}>
      <div className={styles.header}>
        <h1 className={styles.title}>채널</h1>
      </div>
      <div className={styles.list}>
        {!loading && channels.length === 0 && (
          <p className={styles.userBio}>아직 팔로우한 사람이 없습니다.</p>
        )}
        {channels.map((channel) => (
          <div key={channel.userId} className={styles.userRow}>
            <Link href={`/users/${channel.userId}`} className={styles.userLink}>
              <div
                className={styles.avatar}
                style={
                  channel.profileImageUrl
                    ? { backgroundImage: `url(${channel.profileImageUrl})`, backgroundSize: "cover", backgroundPosition: "center" }
                    : undefined
                }
              >
                {!channel.profileImageUrl && channel.nickname.slice(0, 1).toUpperCase()}
              </div>
              <div className={styles.userInfo}>
                <p className={styles.userName}>{channel.nickname}</p>
                <p className={styles.userBio}>{channel.bio || "소개가 없습니다."}</p>
              </div>
            </Link>
            <FollowButton
              targetUserId={channel.userId}
              onFollowChange={(following) => handleFollowChange(channel.userId, following)}
            />
          </div>
        ))}
      </div>
    </main>
  );
}
