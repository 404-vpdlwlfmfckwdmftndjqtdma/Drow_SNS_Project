import api from "./api";
import type { ApiResponse } from "@/types";

interface MediaUploadResponse {
  url: string;
  mediaType: "IMAGE" | "VIDEO";
  sizeBytes: number;
}

// 단일 파일 업로드 (이미지/영상 공용, 최대 50MB). 백엔드가 Cloudinary 로 전달한다.
// FormData 전송 시 Content-Type을 직접 지정하면 boundary가 빠져 서버가 파싱하지 못하므로,
// 브라우저가 자동으로 boundary 포함된 헤더를 설정하도록 헤더를 지정하지 않는다.
export async function uploadMedia(file: File): Promise<MediaUploadResponse> {
  const formData = new FormData();
  formData.append("file", file);
  const res = await api.post<ApiResponse<MediaUploadResponse>>("/api/v1/media/upload", formData);
  return res.data.data;
}

export async function uploadMediaBatch(files: File[]): Promise<MediaUploadResponse[]> {
  const formData = new FormData();
  files.forEach((f) => formData.append("files", f));
  const res = await api.post<ApiResponse<MediaUploadResponse[]>>("/api/v1/media/upload/batch", formData);
  return res.data.data;
}
