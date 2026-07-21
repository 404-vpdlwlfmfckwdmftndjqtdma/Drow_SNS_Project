import { AxiosError } from "axios";
import type { ApiResponse } from "@/types";

/**
 * 백엔드가 내려준 실패 메시지를 그대로 꺼낸다.
 * 결제 테스트에서는 "왜 실패했는지"(잔액 부족 / 이미 구매 / 판매 안 함)가 핵심이라
 * 뭉뚱그린 문구 대신 서버 메시지를 그대로 보여준다.
 */
export function toErrorMessage(error: unknown, fallback = "요청에 실패했습니다."): string {
  const axiosError = error as AxiosError<ApiResponse<unknown>>;
  const serverMessage = axiosError?.response?.data?.message;
  if (serverMessage) {
    return serverMessage;
  }
  if (axiosError?.response?.status === 401) {
    return "로그인이 필요합니다.";
  }
  return fallback;
}
