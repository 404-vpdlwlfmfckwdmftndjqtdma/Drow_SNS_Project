// 채널 상세 + 채널 소속 게시글 피드. TODO: GET /api/v1/channels/{id}
export default async function ChannelDetailPage({ params }: { params: Promise<{ id: string }> }) {
  const { id } = await params;
  return (
    <main>
      <h1>채널 #{id}</h1>
    </main>
  );
}
