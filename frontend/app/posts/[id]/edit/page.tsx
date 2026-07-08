"use client";

// TODO: 기존 게시글 값 불러와 폼 채우기 -> PUT /api/v1/posts/{id}
export default async function EditPostPage({ params }: { params: Promise<{ id: string }> }) {
  const { id } = await params;
  return (
    <main>
      <h1>게시글 수정 #{id}</h1>
    </main>
  );
}
