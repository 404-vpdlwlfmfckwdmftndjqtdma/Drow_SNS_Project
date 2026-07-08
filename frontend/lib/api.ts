import axios from "axios";
import { getAccessToken } from "./auth";

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

// TODO: 401 응답 시 /api/v1/auth/reissue 호출 후 재시도하는 인터셉터 추가

export default api;
