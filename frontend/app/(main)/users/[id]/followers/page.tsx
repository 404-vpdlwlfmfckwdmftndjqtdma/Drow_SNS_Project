"use client";

import { useEffect, useState } from "react";
import Link from "next/link";
import { useParams } from "next/navigation";
import api from "@/lib/api";
import FollowButton from "@/components/follow/FollowButton";
import type { ApiResponse } from "@/types";
import styles from "./page.module.css";

// FollowUserResponse 와 1:1로 맞춘 응답 타입 (channels/page.tsx와 동일).
interface FollowerUser {
  userId: number;
  nickname: string;
  profileImageUrl?: string;
  bio?: string;
}

// 마이페이지/타인 프로필의 "팔로워" 숫자를 눌렀을 때 보이는 목록.
// GET /api/v1/follows/{userId}/followers 로 연동 (로그인 여부와 무관하게 공개, 팔로워 수 자체가 이미 공개 정보라서).
// 항목의 팔로우 버튼은 FollowButton을 재사용 - 채널(팔로잉) 목록 화면과 동일한 패턴.
export default function FollowersPage() {
  const { id } = useParams<{ id: string }>();
  const targetUserId = Number(id);

  const [followers, setFollowers] = useState<FollowerUser[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!Number.isFinite(targetUserId)) return;

    (async () => {
      try {
        const res = await api.get<ApiResponse<FollowerUser[]>>(`/api/v1/follows/${targetUserId}/followers`);
        setFollowers(res.data.data);
      } catch {
        // 실패하면 빈 목록으로 둔다.
      } finally {
        setLoading(false);
      }
    })();
  }, [targetUserId]);

  return (
    <main className={styles.container}>
      <div className={styles.header}>
        <h1 className={styles.title}>팔로워</h1>
      </div>
      <div className={styles.list}>
        {!loading && followers.length === 0 && (
          <p className={styles.userBio}>아직 팔로워가 없습니다.</p>
        )}
        {followers.map((follower) => (
          <div key={follower.userId} className={styles.userRow}>
            <Link href={`/users/${follower.userId}`} className={styles.userLink}>
              <div
                className={styles.avatar}
                style={
                  follower.profileImageUrl
                    ? { backgroundImage: `url(${follower.profileImageUrl})`, backgroundSize: "cover", backgroundPosition: "center" }
                    : undefined
                }
              >
                {!follower.profileImageUrl && follower.nickname.slice(0, 1).toUpperCase()}
              </div>
              <div className={styles.userInfo}>
                <p className={styles.userName}>{follower.nickname}</p>
                <p className={styles.userBio}>{follower.bio || "소개가 없습니다."}</p>
              </div>
            </Link>
            <FollowButton targetUserId={follower.userId} />
          </div>
        ))}
      </div>
    </main>
  );
}
