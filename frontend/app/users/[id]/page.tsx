// 다른 사용자 프로필 + 팔로우 버튼 + 해당 사용자의 게시글 목록.
// TODO: GET /api/v1/users/{id}, POST/DELETE /api/v1/follows/{id}
export default async function UserProfilePage({ params }: { params: Promise<{ id: string }> }) {
  const { id } = await params;
  return (
    <main>
      <h1>사용자 #{id}</h1>
    </main>
  );
}
