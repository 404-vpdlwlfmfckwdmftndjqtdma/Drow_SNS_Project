"use client";

import PurchaseButton from "./PurchaseButton";

/**
 * 이미지 블러 구매 버튼. postId만 넘기면 가격 조회 → (부족분 충전) → 구매까지 알아서 처리한다.
 * capability는 "imageBlur"로 고정이라 쓰는 쪽이 고를 필요가 없다.
 */
export default function ImageBlurPurchaseButton({
  postId,
  onDone,
}: {
  postId: number;
  onDone?: () => void;
}) {
  return <PurchaseButton postId={postId} capability="imageBlur" label="이미지 블러" onDone={onDone} />;
}
