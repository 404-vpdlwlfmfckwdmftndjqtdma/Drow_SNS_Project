import type { Metadata } from "next";
import "../styles/globals.css";

export const metadata: Metadata = {
  title: "CanvasFlow",
  description: "그림·글·영상을 자유롭게 게시하고 소통하는 SNS",
};

// TODO: Header, BottomNav 등 공통 레이아웃 컴포넌트(components/layout) 배치
export default function RootLayout({ children }: { children: React.ReactNode }) {
  return (
    <html lang="ko">
      <body>{children}</body>
    </html>
  );
}
