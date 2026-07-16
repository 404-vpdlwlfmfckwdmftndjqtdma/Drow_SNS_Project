import { Suspense } from "react";
import Sidebar from "@/components/layout/Sidebar";
import TopBar from "@/components/layout/TopBar";
import styles from "./layout.module.css";

// 로그인 이후 화면(피드/게시글/채널/마이페이지/알림/검색/사용자프로필)에 공통으로 적용되는 셸.
// (auth) 라우트 그룹(로그인/회원가입)은 이 레이아웃을 사용하지 않는다.
export default function MainLayout({ children }: { children: React.ReactNode }) {
  return (
    <>
      <Sidebar />
      <Suspense>
        <TopBar />
      </Suspense>
      <main className={styles.main}>{children}</main>
    </>
  );
}
