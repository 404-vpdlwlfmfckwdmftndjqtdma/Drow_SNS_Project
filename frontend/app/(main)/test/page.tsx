"use client";

import PayButton from "@/components/modules/payment/PayButton";

export default function TestPage() {
  return (
    <main>
      <h1>결제 테스트</h1>
      {/* 결제 버튼을 모듈에서 태그로 불러 사용 */}
      <PayButton amount={1000} orderName="테스트 결제" returnUrl="/test">
        1000원 결제하기
      </PayButton>
    </main>
  );
}
