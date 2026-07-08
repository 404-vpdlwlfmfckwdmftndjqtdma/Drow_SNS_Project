"use client";

import { useState } from "react";
import styles from "./page.module.css";

type PostType = "text" | "image" | "video";
type Visibility = "public" | "subscribers" | "private";

// 게시글 작성. TODO: MediaUploader 연동(lib/image.ts) 후 POST /api/v1/posts 호출
export default function NewPostPage() {
  const [postType, setPostType] = useState<PostType>("text");
  const [visibility, setVisibility] = useState<Visibility>("public");

  return (
    <div className={styles.container}>
      <div className={styles.grid}>
        <div>
          <div className={styles.typeSelector}>
            <button
              className={postType === "text" ? styles.typeBtnActive : styles.typeBtn}
              onClick={() => setPostType("text")}
              type="button"
            >
              <span className="material-symbols-outlined" style={{ fontSize: 20 }}>notes</span>
              텍스트
            </button>
            <button
              className={postType === "image" ? styles.typeBtnActive : styles.typeBtn}
              onClick={() => setPostType("image")}
              type="button"
            >
              <span className="material-symbols-outlined" style={{ fontSize: 20 }}>image</span>
              이미지
            </button>
            <button
              className={postType === "video" ? styles.typeBtnActive : styles.typeBtn}
              onClick={() => setPostType("video")}
              type="button"
            >
              <span className="material-symbols-outlined" style={{ fontSize: 20 }}>videocam</span>
              비디오
            </button>
          </div>

          <div className={styles.editor}>
            <textarea className={styles.textarea} placeholder="무슨 생각을 하고 계신가요? 내용을 입력하세요..." />

            {postType !== "text" && (
              <div className={styles.uploadArea}>
                {/* TODO: components/post/MediaUploader 로 교체 (lib/image.ts uploadMediaBatch 사용) */}
                <span className="material-symbols-outlined" style={{ fontSize: 48, opacity: 0.4 }}>cloud_upload</span>
                <p style={{ fontWeight: 700 }}>파일을 드래그하거나 클릭하여 업로드</p>
                <p style={{ fontSize: 14, opacity: 0.7 }}>최대 100MB (JPG, PNG, MP4 등 지원)</p>
              </div>
            )}

            <div className={styles.toolbar}>
              <div className={styles.toolbarIcons}>
                <button type="button"><span className="material-symbols-outlined">mood</span></button>
                <button type="button"><span className="material-symbols-outlined">location_on</span></button>
                <button type="button"><span className="material-symbols-outlined">alternate_email</span></button>
                <button type="button"><span className="material-symbols-outlined">tag</span></button>
              </div>
              <span className={styles.charCount}>0 / 2000 자</span>
            </div>
          </div>
        </div>

        <div>
          <div className={styles.settingsCard}>
            <h3 className={styles.settingsTitle}>
              <span className="material-symbols-outlined" style={{ color: "var(--color-primary)" }}>visibility</span>
              공개 설정
            </h3>

            <label className={visibility === "public" ? styles.radioOptionChecked : styles.radioOption}>
              <input type="radio" name="visibility" checked={visibility === "public"} onChange={() => setVisibility("public")} />
              <div>
                <p className={styles.radioLabel}>전체 공개</p>
                <p className={styles.radioDesc}>누구나 볼 수 있습니다.</p>
              </div>
            </label>

            <label className={visibility === "subscribers" ? styles.radioOptionChecked : styles.radioOption}>
              <input type="radio" name="visibility" checked={visibility === "subscribers"} onChange={() => setVisibility("subscribers")} />
              <div>
                <p className={styles.radioLabel}>구독자 전용</p>
                <p className={styles.radioDesc}>선택한 등급의 구독자만 가능.</p>
              </div>
            </label>

            <label className={visibility === "private" ? styles.radioOptionChecked : styles.radioOption}>
              <input type="radio" name="visibility" checked={visibility === "private"} onChange={() => setVisibility("private")} />
              <div>
                <p className={styles.radioLabel}>나만 보기</p>
                <p className={styles.radioDesc}>비공개로 저장됩니다.</p>
              </div>
            </label>

            {visibility === "subscribers" && (
              <select className={styles.tierSelect} defaultValue="basic">
                <option value="basic">Tier 1 (베이직)</option>
                <option value="premium">Tier 2 (프리미엄)</option>
                <option value="vip">Tier 3 (VIP)</option>
              </select>
            )}
          </div>

          {/* TODO: POST /api/v1/posts 호출 (visibility -> ContentVisibility 매핑: public=PUBLIC, subscribers=BLUR/RESTRICTED 등, private=RESTRICTED) */}
          <button className={styles.submitBtn} type="button">
            <span className="material-symbols-outlined filled">send</span>
            게시하기
          </button>

          <div className={styles.tipCard}>
            <p className={styles.tipTitle}>
              <span className="material-symbols-outlined" style={{ fontSize: 20 }}>lightbulb</span>
              작성 팁
            </p>
            <p className={styles.tipDesc}>
              구독자 전용 게시물은 일반 게시물보다 <strong>2.5배</strong> 더 높은 참여도를 보입니다. 전용 혜택을 강조해보세요!
            </p>
          </div>
        </div>
      </div>
    </div>
  );
}
