import type { OrderConfirmResponse } from "./types";
import PaymentReturnButton from "./PaymentReturnButton";
import styles from "./PaymentStatus.module.css";

/** 충전 완료 결과 표시 (success 페이지가 사용) */
export default function ChargeResult({ data }: { data: OrderConfirmResponse }) {
  return (
    <section className={styles.card} aria-labelledby="charge-success-title">
      <div className={`${styles.statusIcon} ${styles.successIcon}`} aria-hidden="true">
        <span className="material-symbols-outlined filled">check</span>
      </div>

      <p className={styles.eyebrow}>충전 완료</p>
      <h1 className={styles.title} id="charge-success-title">토큰이 충전되었어요</h1>
      <p className={styles.description}>
        {data.alreadyProcessed
          ? "이미 처리된 주문이라 중복 충전 없이 현재 상태를 보여드립니다."
          : "결제 승인과 지갑 적립이 정상적으로 처리되었습니다."}
      </p>

      <div className={styles.amountPanel}>
        <span>충전 금액</span>
        <strong>{data.amount.toLocaleString("ko-KR")}원</strong>
      </div>

      <dl className={styles.details}>
        <div>
          <dt>충전 후 잔액</dt>
          <dd>{data.balance.toLocaleString("ko-KR")}원</dd>
        </div>
        <div>
          <dt>주문번호</dt>
          <dd className={styles.orderId}>{data.orderId}</dd>
        </div>
      </dl>

      <div className={styles.actions}>
        <PaymentReturnButton />
      </div>

      <p className={styles.helpText}>충전한 토큰으로 게시물 상품과 채널을 구매할 수 있습니다.</p>
    </section>
  );
}
