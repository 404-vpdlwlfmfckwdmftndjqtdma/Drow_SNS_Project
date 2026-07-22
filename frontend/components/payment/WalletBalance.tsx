"use client";

import { useCallback, useEffect, useState } from "react";
import api from "@/lib/api";
import type { ApiResponse } from "@/types";
import type { WalletBalance as WalletBalanceData } from "./types";
import { toErrorMessage } from "./errorMessage";
import styles from "./PaymentPanel.module.css";

/**
 * [보유 토큰] 지갑 잔액 표시.
 * 충전·구매 후 갱신해야 하므로, 부모가 refreshKey를 올려 다시 조회하게 한다.
 */
export default function WalletBalance({ refreshKey = 0 }: { refreshKey?: number }) {
  const [balance, setBalance] = useState<number | null>(null);
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(true);

  const load = useCallback(() => {
    setLoading(true);
    setError("");
    api
      .get<ApiResponse<WalletBalanceData>>("/api/v1/wallet/me")
      .then((res) => setBalance(res.data.data.balance))
      .catch((err) => setError(toErrorMessage(err, "잔액을 불러오지 못했습니다.")))
      .finally(() => setLoading(false));
  }, []);

  useEffect(() => {
    load();
  }, [load, refreshKey]);

  return (
    <section className={styles.card}>
      <div className={styles.cardHeader}>
        <h2 className={styles.cardTitle}>보유 토큰</h2>
      </div>

      {loading ? (
        <p className={styles.info}>불러오는 중...</p>
      ) : error ? (
        <p className={styles.error}>{error}</p>
      ) : (
        <p className={styles.success}>
          {balance?.toLocaleString("ko-KR")}원
        </p>
      )}

      <button type="button" className={styles.secondaryButton} onClick={load}>
        새로고침
      </button>
    </section>
  );
}
