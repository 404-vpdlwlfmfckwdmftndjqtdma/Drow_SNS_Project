import api from "./api";
import { clearTokens } from "./auth";

// 로그아웃: 서버에 리프레시 토큰 폐기를 요청한 뒤 로컬 토큰을 지운다.
// 서버 요청이 실패해도(네트워크 오류 등) 로컬 토큰은 반드시 지운다.
export async function logout(): Promise<void> {
  try {
    await api.post("/api/v1/auth/logout");
  } catch {
    // 이미 만료된 토큰이었거나 네트워크 오류인 경우도 로컬 로그아웃은 진행한다.
  } finally {
    clearTokens();
  }
}
