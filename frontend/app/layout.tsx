import type { Metadata } from "next";
import "../styles/globals.css";

export const metadata: Metadata = {
  title: "404 Not Found (4nf)",
  description: "그림·글·영상을 자유롭게 게시하고 소통하는 SNS",
};

export default function RootLayout({ children }: { children: React.ReactNode }) {
  return (
    <html lang="ko">
      <head>
        <link rel="preconnect" href="https://fonts.googleapis.com" />
        <link
          href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&family=Sora:wght@600;700;800&display=swap"
          rel="stylesheet"
        />
        {/* 아이콘 폰트는 display=block: 로드 전에 리가처 텍스트(grid_view 등)가 노출되어 레이아웃이 밀리는 것 방지 */}
        <link
          href="https://fonts.googleapis.com/css2?family=Material+Symbols+Outlined:wght,FILL@100..700,0..1&display=block"
          rel="stylesheet"
        />
      </head>
      <body>{children}</body>
    </html>
  );
}
