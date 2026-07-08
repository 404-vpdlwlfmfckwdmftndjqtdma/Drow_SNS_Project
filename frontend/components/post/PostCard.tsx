import type { PostSummary } from "@/types";

// 목록/피드에 쓰이는 게시글 카드. locked=true 면 썸네일을 블러 처리.
export default function PostCard({ post }: { post: PostSummary }) {
  return (
    <article>
      <h3>{post.title}</h3>
      {/* TODO: 썸네일, 좋아요/댓글/조회수, locked 표시 */}
    </article>
  );
}
