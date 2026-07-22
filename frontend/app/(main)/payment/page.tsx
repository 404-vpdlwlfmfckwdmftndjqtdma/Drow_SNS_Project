import ChargeButton from "@/components/payment/ChargeButton";
import WalletBalance from "@/components/payment/WalletBalance";
import styles from "./page.module.css";

export default function PaymentPage() {
  return (
    <div className={styles.page}>
      <header className={styles.header}>
        <p className={styles.eyebrow}>PAYMENT</p>
        <h1 className={styles.title}>결제</h1>
        <p className={styles.description}>
          보유 토큰 잔액을 확인하고 필요한 만큼 충전할 수 있습니다.
        </p>
      </header>

      <div className={styles.grid}>
        <WalletBalance />
        <ChargeButton returnUrl="/payment" />
      </div>
    </div>
  );
}
