// 게시글 상세. locked=true 인 경우 components/subscription/ContentGate 로
// 공개 범위(블러/블랙박스/접근제한/부분공개)에 맞춰 마스킹 UI를 보여준다.
// TODO: GET /api/v1/posts/{id} 호출
export default async function PostDetailPage({ params }: { params: Promise<{ id: string }> }) {
  const { id } = await params;
  return (
    <main>
      <h1>게시글 #{id}</h1>
    </main>
  );
}
