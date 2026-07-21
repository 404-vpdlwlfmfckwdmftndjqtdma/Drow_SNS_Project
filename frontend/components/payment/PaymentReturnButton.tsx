"use client";

import { useRouter } from "next/navigation";
import { PAYMENT_RETURN_URL_KEY } from "./config";
import styles from "./PaymentStatus.module.css";

const FALLBACK_URL = "/posts";

/** 토스 결제를 시작하기 직전에 저장한 4nf 페이지로 돌아간다. */
export default function PaymentReturnButton() {
  const router = useRouter();

  const handleReturn = () => {
    const savedUrl = sessionStorage.getItem(PAYMENT_RETURN_URL_KEY);
    sessionStorage.removeItem(PAYMENT_RETURN_URL_KEY);

    if (!savedUrl) {
      router.replace(FALLBACK_URL);
      return;
    }

    try {
      const target = new URL(savedUrl, window.location.origin);
      if (target.origin !== window.location.origin) {
        router.replace(FALLBACK_URL);
        return;
      }

      router.replace(`${target.pathname}${target.search}${target.hash}`);
    } catch {
      router.replace(FALLBACK_URL);
    }
  };

  return (
    <button className={styles.primaryAction} type="button" onClick={handleReturn}>
      결제를 시작한 페이지로 돌아가기
    </button>
  );
}
