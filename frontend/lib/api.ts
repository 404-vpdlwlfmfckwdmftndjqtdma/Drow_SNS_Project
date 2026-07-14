import axios from "axios";
import { getAccessToken, getRefreshToken, setTokens, clearTokens } from "./auth";

// 모든 API 호출은 이 인스턴스를 사용한다. 토큰이 자동으로 Authorization 헤더에 첨부된다.
const api = axios.create({
  baseURL: process.env.NEXT_PUBLIC_API_URL,
});

api.interceptors.request.use((config) => {
  const token = getAccessToken();
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// 재발급 요청은 api 인터셉터를 타지 않는 별도 인스턴스로 보낸다 (무한 루프 방지).
const reissueClient = axios.create({
  baseURL: process.env.NEXT_PUBLIC_API_URL,
});

// 401 응답을 받으면 refreshToken으로 재발급 후 원 요청을 한 번만 재시도한다.
// /api/v1/auth/** 요청 자체의 401(로그인 실패 등)은 재발급 대상이 아니므로 그대로 통과시킨다.
api.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config;

    if (
      error.response?.status !== 401 ||
      originalRequest?._retry ||
      originalRequest?.url?.includes("/api/v1/auth/")
    ) {
      return Promise.reject(error);
    }

    const refreshToken = getRefreshToken();
    if (!refreshToken) {
      clearTokens();
      return Promise.reject(error);
    }

    originalRequest._retry = true;

    try {
      const { data } = await reissueClient.post("/api/v1/auth/reissue", { refreshToken });
      const { accessToken, refreshToken: newRefreshToken } = data.data;
      setTokens(accessToken, newRefreshToken);
      originalRequest.headers.Authorization = `Bearer ${accessToken}`;
      return api(originalRequest);
    } catch (reissueError) {
      clearTokens();
      if (typeof window !== "undefined") {
        window.location.href = "/login";
      }
      return Promise.reject(reissueError);
    }
  }
);

export default api;
