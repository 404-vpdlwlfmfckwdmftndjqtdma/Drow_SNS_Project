"use client";

import styles from "./page.module.css";

// 닉네임/프로필 이미지 수정.
// TODO: PATCH /api/v1/users/me/nickname, PATCH /api/v1/users/me/profile-image
export default function EditProfilePage() {
  return (
    <main className={styles.container}>
      <h1 className={styles.title}>프로필 수정</h1>
      {/* TODO: components/mypage/ProfileEditForm */}
    </main>
  );
}
