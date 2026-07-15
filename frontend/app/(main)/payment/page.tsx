import PayButton from "@/components/modules/payment/PayButton";
import styles from "./page.module.css";

const PAYMENT_AMOUNT = 1_000;

export default function PaymentPage() {
  return (
    <div className={styles.page}>
      <header className={styles.header}>
        <p className={styles.eyebrow}>PAYMENT</p>
        <h1 className={styles.title}>결제</h1>
        <p className={styles.description}>안전한 결제를 위해 결제 금액을 확인한 뒤 진행해 주세요.</p>
      </header>

      <section className={styles.card} aria-labelledby="payment-order-title">
        <div className={styles.iconWrap}>
          <span className="material-symbols-outlined">credit_card</span>
        </div>

        <div className={styles.orderInfo}>
          <div>
            <p className={styles.orderLabel}>결제 항목</p>
            <h2 className={styles.orderTitle} id="payment-order-title">4NF 결제</h2>
          </div>
          <strong className={styles.amount}>{PAYMENT_AMOUNT.toLocaleString("ko-KR")}원</strong>
        </div>

        <div className={styles.divider} />

        <div className={styles.payButtonWrap}>
          <PayButton amount={PAYMENT_AMOUNT} orderName="4NF 결제" returnUrl="/payment">
            {PAYMENT_AMOUNT.toLocaleString("ko-KR")}원 결제하기
          </PayButton>
        </div>

        <p className={styles.notice}>결제 버튼을 누르면 카드 결제 화면으로 이동합니다.</p>
      </section>
    </div>
  );
}
