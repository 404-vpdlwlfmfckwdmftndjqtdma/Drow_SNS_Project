"use client";

import { useCallback, useEffect, useState } from "react";
import Link from "next/link";
import api from "@/lib/api";
import { AUTH_CHANGE_EVENT, isLoggedIn } from "@/lib/auth";
import { WALLET_CHANGED_EVENT } from "@/lib/uiEvents";
import type { WalletBalance } from "./types";
import type { ApiResponse } from "@/types";
import styles from "./WalletChip.module.css";

/**
 * 상단바에 보이는 보유 토큰 칩. 누르면 충전(/payment)으로 간다.
 * 충전·구매·구독으로 잔액이 바뀌면 WALLET_CHANGED_EVENT로 다시 조회하고,
 * 토스에서 돌아오는 등 창 포커스가 잡힐 때도 갱신한다.
 */
export default function WalletChip() {
  const [balance, setBalance] = useState<number | null>(null);
  const [visible, setVisible] = useState(false);

  const load = useCallback(async () => {
    if (!isLoggedIn()) {
      setVisible(false);
      return;
    }
    setVisible(true);
    try {
      const res = await api.get<ApiResponse<WalletBalance>>("/api/v1/wallet/me");
      setBalance(res.data.data.balance);
    } catch {
      // 조회 실패 시 금액만 감추고 칩은 유지(클릭하면 충전으로 이동)
    }
  }, []);

  useEffect(() => {
    load();
    const onFocus = () => load();
    window.addEventListener(WALLET_CHANGED_EVENT, load);
    window.addEventListener(AUTH_CHANGE_EVENT, load);
    window.addEventListener("focus", onFocus);
    return () => {
      window.removeEventListener(WALLET_CHANGED_EVENT, load);
      window.removeEventListener(AUTH_CHANGE_EVENT, load);
      window.removeEventListener("focus", onFocus);
    };
  }, [load]);

  if (!visible) return null;

  return (
    <Link href="/payment" className={styles.chip} title="충전하기">
      <span className={`material-symbols-outlined ${styles.coin}`}>monetization_on</span>
      <span className={styles.amount}>
        {balance == null ? "—" : balance.toLocaleString("ko-KR")}
      </span>
    </Link>
  );
}
