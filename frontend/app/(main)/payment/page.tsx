"use client";

import { useState } from "react";
import ChargeButton from "@/components/payment/ChargeButton";
import PurchaseButton from "@/components/payment/PurchaseButton";
import SubscribeButton from "@/components/payment/SubscribeButton";
import WalletBalance from "@/components/payment/WalletBalance";
import styles from "./page.module.css";

/**
 * 결제 테스트 화면.
 * 모든 결제는 지갑을 거친다 - 토스 결제는 "충전"에서만 일어나고,
 * 상품/구독 구매는 충전된 잔액에서 차감된다.
 */
export default function PaymentPage() {
  // 충전·구매 후 잔액을 다시 불러오기 위한 신호
  const [refreshKey, setRefreshKey] = useState(0);
  const refreshBalance = () => setRefreshKey((key) => key + 1);

  return (
    <div className={styles.page}>
      <header className={styles.header}>
        <p className={styles.eyebrow}>PAYMENT</p>
        <h1 className={styles.title}>결제</h1>
        <p className={styles.description}>
          충전한 토큰으로 개별 상품과 채널을 구매합니다. 외부 결제는 충전에서만 일어납니다.
        </p>
      </header>

      <div className={styles.grid}>
        <WalletBalance refreshKey={refreshKey} />

        <ChargeButton returnUrl="/payment" />

        <SubscribeButton onDone={refreshBalance} />

        <PurchaseButton
          capability="textBlur"
          title="텍스트 블러 구매"
          description="그 글의 가려진 본문만 해제합니다."
          onDone={refreshBalance}
        />

        <PurchaseButton
          capability="imageBlur"
          title="이미지 블러 구매"
          description="그 글의 가려진 사진만 해제합니다."
          onDone={refreshBalance}
        />
      </div>
    </div>
  );
}
