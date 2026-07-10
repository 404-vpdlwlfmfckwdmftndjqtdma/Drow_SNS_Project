import type { PaymentResultData } from "./confirmPayment";
import PaymentReturnButton from "./PaymentReturnButton";
import styles from "./PaymentStatus.module.css";

/** 결제 결과 표시 컴포넌트 (success 페이지가 사용) */
export default function PaymentResult({ data }: { data: PaymentResultData }) {
  return (
    <section className={styles.card} aria-labelledby="payment-success-title">
      <div className={`${styles.statusIcon} ${styles.successIcon}`} aria-hidden="true">
        <span className="material-symbols-outlined filled">check</span>
      </div>

      <p className={styles.eyebrow}>결제 완료</p>
      <h1 className={styles.title} id="payment-success-title">결제가 완료되었어요</h1>
      <p className={styles.description}>결제 승인이 정상적으로 처리되었습니다.</p>

      <div className={styles.amountPanel}>
        <span>최종 결제 금액</span>
        <strong>{data.amount.toLocaleString("ko-KR")}원</strong>
      </div>

      <dl className={styles.details}>
        <div>
          <dt>주문명</dt>
          <dd>{data.orderName}</dd>
        </div>
        <div>
          <dt>결제수단</dt>
          <dd>{data.method || "확인 중"}</dd>
        </div>
        <div>
          <dt>승인시각</dt>
          <dd>{formatApprovedAt(data.approvedAt)}</dd>
        </div>
        <div>
          <dt>주문번호</dt>
          <dd className={styles.orderId}>{data.orderId}</dd>
        </div>
      </dl>

      <div className={styles.actions}>
        <PaymentReturnButton />
      </div>

      <p className={styles.helpText}>결제 내역은 마이페이지에서 다시 확인할 수 있습니다.</p>
    </section>
  );
}

function formatApprovedAt(value: string) {
  const approvedAt = new Date(value);
  if (Number.isNaN(approvedAt.getTime())) return value;

  return new Intl.DateTimeFormat("ko-KR", {
    dateStyle: "medium",
    timeStyle: "short",
  }).format(approvedAt);
}
