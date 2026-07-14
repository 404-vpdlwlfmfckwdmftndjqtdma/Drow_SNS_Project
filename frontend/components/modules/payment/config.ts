// 토스 clientKey (프론트 공개키). .env.local 의 NEXT_PUBLIC_TOSS_CLIENT_KEY 로 주입.
// 값이 없으면 토스 공개 테스트 키로 폴백 (실 서비스에선 env로 반드시 지정).
export const TOSS_CLIENT_KEY =
  process.env.NEXT_PUBLIC_TOSS_CLIENT_KEY ?? "test_ck_D5GePWvyJnrK0W0k6q8gLzN97Eoq";

// 결제 후 토스가 돌려보낼 라우트 (리다이렉트 착지 페이지)
export const PAYMENT_SUCCESS_PATH = "/payment/success";
export const PAYMENT_FAIL_PATH = "/payment/fail";

// 결제를 시작한 4nf 페이지로 돌아가기 위한 세션 저장 키
export const PAYMENT_RETURN_URL_KEY = "4nf_payment_return_url";
