"use client";

import styles from "./ProfileEditForm.module.css";

// TODO: 닉네임 입력 + 프로필 이미지 업로드(lib/image.ts) -> UserService 관련 API 호출
export default function ProfileEditForm() {
  return (
    <form className={styles.form}>
      <div className={styles.avatarRow}>
        <div className={styles.avatar} />
        {/* TODO: 프로필 이미지 업로드 버튼 */}
      </div>
      <input className={styles.input} type="text" placeholder="닉네임" />
      <button className={styles.submit} type="submit">저장</button>
    </form>
  );
}
