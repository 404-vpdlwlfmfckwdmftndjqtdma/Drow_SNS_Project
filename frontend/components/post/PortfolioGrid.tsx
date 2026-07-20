import Link from "next/link";
import styles from "./PortfolioGrid.module.css";

// 마이페이지/다른 사람 프로필(users/[id]) 공용 게시글 그리드 카드 데이터 형태.
export interface PortfolioPost {
  postId: number;
  title: string;
  content?: string;
  thumbnailUrl?: string;
  hasMedia: boolean;
  isVideo?: boolean;
}

// 백엔드 MyPagePostResponse 와 1:1로 맞춘 응답 타입
// (GET /api/v1/mypage/posts, GET /api/v1/mypage/{userId}/posts 공용).
export interface MyPagePostResponse {
  postId: number;
  content: string;
  thumbnailUrl?: string;
  hasMedia: boolean;
  isVideo: boolean;
}

const TITLE_MAX_LENGTH = 24;

// post 도메인에는 title이 없어서(글 하나가 최대 800자 짧은 텍스트), content 앞부분을 제목처럼 잘라 쓰고
// 나머지가 남으면 그 전체를 본문 스니펫으로 같이 보여준다. 짧은 글은 제목만 쓰고 스니펫은 생략한다.
export function toPortfolioPosts(rawPosts: MyPagePostResponse[]): PortfolioPost[] {
  return rawPosts.map((post) => {
    const content = post.content ?? "";
    const isLong = content.length > TITLE_MAX_LENGTH;
    return {
      postId: post.postId,
      title: isLong ? `${content.slice(0, TITLE_MAX_LENGTH)}…` : content,
      content: isLong ? content : undefined,
      thumbnailUrl: post.thumbnailUrl,
      hasMedia: post.hasMedia,
      isVideo: post.isVideo,
    };
  });
}

interface PortfolioGridProps {
  posts: PortfolioPost[];
  wideFirst?: boolean;
  isOwner?: boolean;
}

// 사진/영상이 있으면 첫 번째 미디어 썸네일만, 없으면 텍스트(제목+본문 일부)만 보여준다.
// 좋아요 수는 어느 카드에도 노출하지 않는다 - mypage/users[id] 양쪽에서 공용으로 쓴다.
// 카드 전체가 게시글 상세(/posts/{postId})로 이동하는 링크다.
// posts가 비어있으면 본인/타인 여부(isOwner)에 따라 다른 빈 상태를 보여준다.
export default function PortfolioGrid({ posts, wideFirst, isOwner }: PortfolioGridProps) {
  if (posts.length === 0) {
    return (
      <div className={styles.empty}>
        <span className="material-symbols-outlined" style={{ fontSize: 32, opacity: 0.5 }}>
          {isOwner ? "add_photo_alternate" : "photo_library"}
        </span>
        {isOwner ? (
          <>
            <p className={styles.emptyTitle}>아직 창작물이 없어요</p>
            <p className={styles.emptyBody}>첫 작품을 올려서 포트폴리오를 채워보세요.</p>
            <Link href="/posts/new" className={styles.emptyBtn}>
              <span className="material-symbols-outlined filled" style={{ fontSize: 18 }}>add</span>
              게시글 작성하기
            </Link>
          </>
        ) : (
          <p className={styles.emptyTitle}>아직 등록된 창작물이 없습니다.</p>
        )}
      </div>
    );
  }

  return (
    <div className={styles.grid}>
      {posts.map((post, index) => (
        <Link
          key={post.postId}
          href={`/posts/${post.postId}`}
          className={`${styles.card} ${wideFirst && index === 0 ? styles.cardWide : ""}`}
        >
          {post.hasMedia ? (
            <div
              className={styles.media}
              style={post.thumbnailUrl ? { backgroundImage: `url(${post.thumbnailUrl})` } : undefined}
            >
              {!post.thumbnailUrl && (
                <span className="material-symbols-outlined" style={{ fontSize: 28, opacity: 0.5 }}>
                  {post.isVideo ? "movie" : "image"}
                </span>
              )}
              {post.isVideo && post.thumbnailUrl && (
                <span className={styles.playOverlay}>
                  <span className="material-symbols-outlined filled" style={{ fontSize: 40 }}>play_circle</span>
                </span>
              )}
            </div>
          ) : (
            <div className={styles.textCard}>
              <h3 className={styles.title}>{post.title}</h3>
              {post.content && <p className={styles.snippet}>{post.content}</p>}
            </div>
          )}
        </Link>
      ))}
    </div>
  );
}
