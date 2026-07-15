// 로컬스토리지 기반 토큰 저장/삭제 (JWT).
// TODO: 보안 요구사항이 커지면 httpOnly 쿠키 방식으로 전환 검토.

const ACCESS_TOKEN_KEY = "cf_access_token";
const REFRESH_TOKEN_KEY = "cf_refresh_token";
export const AUTH_CHANGE_EVENT = "cf-auth-changed";

export function getAccessToken(): string | null {
  if (typeof window === "undefined") return null;
  return localStorage.getItem(ACCESS_TOKEN_KEY);
}

export function getRefreshToken(): string | null {
  if (typeof window === "undefined") return null;
  return localStorage.getItem(REFRESH_TOKEN_KEY);
}

export function setTokens(accessToken: string, refreshToken: string) {
  localStorage.setItem(ACCESS_TOKEN_KEY, accessToken);
  localStorage.setItem(REFRESH_TOKEN_KEY, refreshToken);
  if (typeof window !== "undefined") {
    window.dispatchEvent(new Event(AUTH_CHANGE_EVENT));
  }
}

export function clearTokens() {
  localStorage.removeItem(ACCESS_TOKEN_KEY);
  localStorage.removeItem(REFRESH_TOKEN_KEY);
  if (typeof window !== "undefined") {
    window.dispatchEvent(new Event(AUTH_CHANGE_EVENT));
  }
}

export function isLoggedIn(): boolean {
  return !!getAccessToken();
}

// Access token의 payload(sub)에 담긴 userId를 읽는다.
// 파싱 실패/토큰 없음이면 null을 반환한다.
export function getCurrentUserId(): number | null {
  const token = getAccessToken();
  if (!token) return null;

  try {
    const parts = token.split(".");
    if (parts.length < 2) return null;

    const payload = parts[1]
      .replace(/-/g, "+")
      .replace(/_/g, "/")
      .padEnd(Math.ceil(parts[1].length / 4) * 4, "=");

    const decoded = JSON.parse(atob(payload)) as { sub?: string };
    const userId = Number(decoded.sub);
    return Number.isFinite(userId) ? userId : null;
  } catch {
    return null;
  }
}
