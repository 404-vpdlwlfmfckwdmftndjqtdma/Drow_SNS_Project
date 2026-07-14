"use client";

import { useEffect, useState, type ChangeEvent, type FormEvent } from "react";
import { useRouter } from "next/navigation";
import axios from "axios";
import api from "@/lib/api";
import { uploadMedia } from "@/lib/image";
import type { ApiResponse, User } from "@/types";
import styles from "./ProfileEditForm.module.css";

// 닉네임/소개 입력 + 프로필 이미지 업로드(lib/image.ts) -> PATCH /api/v1/users/me/nickname, /me/bio, /me/profile-image
export default function ProfileEditForm() {
  const router = useRouter();
  const [nickname, setNickname] = useState("");
  const [bio, setBio] = useState("");
  const [profileImageUrl, setProfileImageUrl] = useState("");
  const [loading, setLoading] = useState(true);
  const [uploading, setUploading] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState("");

  useEffect(() => {
    (async () => {
      try {
        const res = await api.get<ApiResponse<User>>("/api/v1/users/me");
        const me = res.data.data;
        setNickname(me.nickname);
        setBio(me.bio ?? "");
        setProfileImageUrl(me.profileImageUrl ?? "");
      } catch {
        setError("내 정보를 불러오지 못했습니다.");
      } finally {
        setLoading(false);
      }
    })();
  }, []);

  const handleImageChange = async (e: ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    e.target.value = "";
    if (!file) return;
    setUploading(true);
    setError("");
    try {
      const uploaded = await uploadMedia(file);
      setProfileImageUrl(uploaded.url);
    } catch {
      setError("이미지 업로드에 실패했습니다.");
    } finally {
      setUploading(false);
    }
  };

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    setError("");
    setSubmitting(true);
    try {
      await api.patch("/api/v1/users/me/nickname", { nickname });
      await api.patch("/api/v1/users/me/bio", { bio });
      if (profileImageUrl) {
        await api.patch("/api/v1/users/me/profile-image", { profileImageUrl });
      }
      router.push("/mypage");
    } catch (err) {
      const message = axios.isAxiosError(err)
        ? (err.response?.data as { message?: string } | undefined)?.message
        : undefined;
      setError(message ?? "프로필 수정에 실패했습니다.");
    } finally {
      setSubmitting(false);
    }
  };

  if (loading) {
    return <p className={styles.loading}>불러오는 중...</p>;
  }

  return (
    <form className={styles.form} onSubmit={handleSubmit}>
      <div className={styles.avatarRow}>
        <div
          className={styles.avatar}
          style={profileImageUrl ? { backgroundImage: `url(${profileImageUrl})` } : undefined}
        />
        <label className={styles.uploadBtn}>
          {uploading ? "업로드 중..." : "이미지 변경"}
          <input
            className={styles.fileInput}
            type="file"
            accept="image/*"
            onChange={handleImageChange}
            disabled={uploading}
          />
        </label>
      </div>

      <div className={styles.field}>
        <label className={styles.label} htmlFor="nickname">닉네임</label>
        <input
          className={styles.input}
          id="nickname"
          type="text"
          value={nickname}
          onChange={(e) => setNickname(e.target.value)}
          minLength={2}
          maxLength={20}
          required
        />
      </div>

      <div className={styles.field}>
        <label className={styles.label} htmlFor="bio">소개</label>
        <textarea
          className={styles.textarea}
          id="bio"
          value={bio}
          onChange={(e) => setBio(e.target.value)}
          maxLength={150}
          rows={3}
          placeholder="자신을 소개해보세요"
        />
      </div>

      {error && <p className={styles.errorText}>{error}</p>}

      <button className={styles.submit} type="submit" disabled={submitting || uploading}>
        {submitting ? "저장 중..." : "저장"}
      </button>
    </form>
  );
}
