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
      <div data-layout="post-form">
        <div>
          <div data-panel="type-selector">
            <button
              data-active={postType === "text" ? "true" : undefined}
              onClick={() => setPostType("text")}
              type="button"
            >
              <span data-icon>notes</span>
              텍스트
            </button>
            <button
              data-active={postType === "image" ? "true" : undefined}
              onClick={() => setPostType("image")}
              type="button"
            >
              <span data-icon>image</span>
              이미지
            </button>
            <button
              data-active={postType === "video" ? "true" : undefined}
              onClick={() => setPostType("video")}
              type="button"
            >
              <span data-icon>videocam</span>
              비디오
            </button>
          </div>

          <div data-panel="editor">
            <textarea placeholder="무슨 생각을 하고 계신가요? 내용을 입력하세요..." />

            {postType !== "text" && (
              <div data-panel="upload">
                {/* TODO: components/post/MediaUploader 로 교체 (lib/image.ts uploadMediaBatch 사용) */}
                <span data-icon data-size="large">cloud_upload</span>
                <p>파일을 드래그하거나 클릭하여 업로드</p>
                <p>최대 100MB (JPG, PNG, MP4 등 지원)</p>
              </div>
            )}

            <div data-toolbar>
              <div>
                <button type="button"><span data-icon>mood</span></button>
                <button type="button"><span data-icon>location_on</span></button>
                <button type="button"><span data-icon>alternate_email</span></button>
                <button type="button"><span data-icon>tag</span></button>
              </div>
              <span>0 / 2000 자</span>
            </div>
          </div>
        </div>

        <div>
          <div data-panel="settings">
            <h3>
              <span data-icon data-tone="primary">visibility</span>
              공개 설정
            </h3>

            <label>
              <input type="radio" name="visibility" checked={visibility === "public"} onChange={() => setVisibility("public")} />
              <div>
                <p>전체 공개</p>
                <p>누구나 볼 수 있습니다.</p>
              </div>
            </label>

            <label>
              <input type="radio" name="visibility" checked={visibility === "subscribers"} onChange={() => setVisibility("subscribers")} />
              <div>
                <p>구독자 전용</p>
                <p>선택한 등급의 구독자만 가능.</p>
              </div>
            </label>

            <label>
              <input type="radio" name="visibility" checked={visibility === "private"} onChange={() => setVisibility("private")} />
              <div>
                <p>나만 보기</p>
                <p>비공개로 저장됩니다.</p>
              </div>
            </label>

            {visibility === "subscribers" && (
              <select defaultValue="basic">
                <option value="basic">Tier 1 (베이직)</option>
                <option value="premium">Tier 2 (프리미엄)</option>
                <option value="vip">Tier 3 (VIP)</option>
              </select>
            )}
          </div>

          {/* TODO: POST /api/v1/posts 호출 (visibility -> ContentVisibility 매핑: public=PUBLIC, subscribers=BLUR/RESTRICTED 등, private=RESTRICTED) */}
          <button data-action="submit" type="button">
            <span data-icon data-filled="true">send</span>
            게시하기
          </button>

          <div data-panel="tip">
            <p>
              <span data-icon>lightbulb</span>
              작성 팁
            </p>
            <p>
              구독자 전용 게시물은 일반 게시물보다 <strong>2.5배</strong> 더 높은 참여도를 보입니다. 전용 혜택을 강조해보세요!
            </p>
          </div>
        </div>
      </div>
    </div>
  );
}
