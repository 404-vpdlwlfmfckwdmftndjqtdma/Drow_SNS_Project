"use client";

import ProfileEditForm from "@/components/mypage/ProfileEditForm";
import styles from "./page.module.css";

// 닉네임/프로필 이미지 수정.
export default function EditProfilePage() {
  return (
    <main className={styles.container}>
      <h1 className={styles.title}>프로필 수정</h1>
      <ProfileEditForm />
    </main>
  );
}
