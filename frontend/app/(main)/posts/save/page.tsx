"use client";

import { FormEvent, useState } from "react";
import { useSaveText } from "@/lib/postStore";
import api from "@/lib/api";
import styles from "./page.module.css";

export default function PostSavePage() {
  const [text, setText] = useSaveText();
  const [blurStart, setBlurStart] = useState("");
  const [blurEnd, setBlurEnd] = useState("");

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();

    const hasBlur = blurStart !== "" && blurEnd !== "";
    const body = {
      text,
      imageIds: [],
      videoIds: [],
      extensions: hasBlur
        ? { textBlur: { ranges: [{ start: Number(blurStart), end: Number(blurEnd) }] } }
        : {},
    };

    try {
      const res = await api.post<number>("/api/v1/post", body);
      alert(`저장 완료 (postId: ${res.data})`);
    } catch {
      alert("전송에 실패했습니다.");
    }
  };

  return (
    <main className={styles.container}>
      <form onSubmit={handleSubmit}>
        <input
          onChange={(event) => setText(event.target.value)}
          placeholder="텍스트를 입력하세요"
          type="text"
          value={text}
        />
        <div>
          <input
            onChange={(event) => setBlurStart(event.target.value)}
            placeholder="블러 시작 번호"
            type="number"
            value={blurStart}
          />
          <input
            onChange={(event) => setBlurEnd(event.target.value)}
            placeholder="블러 끝 번호"
            type="number"
            value={blurEnd}
          />
        </div>
        <button type="submit">전송</button>
      </form>
    </main>
  );
}
