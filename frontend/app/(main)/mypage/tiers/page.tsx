"use client";

import { useCallback, useEffect, useState } from "react";
import api from "@/lib/api";
import { getCurrentUserId } from "@/lib/auth";
import { toErrorMessage } from "@/components/payment/errorMessage";
import type { TierResponse } from "@/components/payment/types";
import styles from "./page.module.css";


/**
 * 구독 등록 - 작가가 자기 채널에서 팔 구독 상품(이름 + 월 금액)을 만든다.
 *
 * 여기서 만든 상품이 프로필의 "구독" 버튼 모달에 그대로 뜨고,
 * 구독자가 고르면 그 금액만큼 지갑에서 차감된다.
 * 내가 "구독한" 목록(/mypage/subscriptions)과는 반대 방향의 화면이다.
 */
export default function TierManagePage() {
  const [tiers, setTiers] = useState<TierResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [message, setMessage] = useState("");

  // 새 상품 입력
  const [name, setName] = useState("");
  const [price, setPrice] = useState("");
  const [description, setDescription] = useState("");
  const [saving, setSaving] = useState(false);

  // 수정 중인 상품 (id를 들고 있으면 그 행이 편집 모드)
  const [editingId, setEditingId] = useState<number | null>(null);
  const [editName, setEditName] = useState("");
  const [editPrice, setEditPrice] = useState("");
  const [editDescription, setEditDescription] = useState("");

  // 목록 조회는 공개 API를 쓴다 - 내 채널 = 내 userId
  const load = useCallback(async () => {
    const myId = getCurrentUserId();
    if (myId == null) {
      setLoading(false);
      return;
    }
    setLoading(true);
    try {
      const res = await api.get<TierResponse[]>(`/api/v1/channels/${myId}/tiers`);
      setTiers(res.data);
    } catch (err) {
      setError(toErrorMessage(err, "구독 상품을 불러오지 못했습니다."));
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    load();
  }, [load]);

  const handleCreate = async () => {
    const value = Number(price);
    if (!name.trim()) {
      setError("상품 이름을 입력하세요.");
      return;
    }
    if (!Number.isInteger(value) || value <= 0) {
      setError("금액은 0보다 큰 정수여야 합니다.");
      return;
    }

    setSaving(true);
    setError("");
    setMessage("");
    try {
      // 채널은 서버가 로그인 유저로 정한다 - 남의 채널에 만들 수 없는 구조
      await api.post("/api/v1/channels/me/tiers", {
        name: name.trim(),
        monthlyPrice: value,
        description: description.trim() || null,
      });
      setName("");
      setPrice("");
      setDescription("");
      setMessage("구독 상품을 등록했습니다.");
      await load();
    } catch (err) {
      setError(toErrorMessage(err, "등록에 실패했습니다."));
    } finally {
      setSaving(false);
    }
  };

  const startEdit = (tier: TierResponse) => {
    setEditingId(tier.id);
    setEditName(tier.name);
    setEditPrice(String(tier.monthlyPrice));
    setEditDescription(tier.description ?? "");
    setError("");
    setMessage("");
  };

  const handleUpdate = async (tierId: number) => {
    const value = Number(editPrice);
    if (!editName.trim()) {
      setError("상품 이름을 입력하세요.");
      return;
    }
    if (!Number.isInteger(value) || value <= 0) {
      setError("금액은 0보다 큰 정수여야 합니다.");
      return;
    }

    setSaving(true);
    setError("");
    try {
      await api.put(`/api/v1/channels/me/tiers/${tierId}`, {
        name: editName.trim(),
        monthlyPrice: value,
        description: editDescription.trim() || null,
      });
      setEditingId(null);
      setMessage("수정했습니다.");
      await load();
    } catch (err) {
      setError(toErrorMessage(err, "수정에 실패했습니다."));
    } finally {
      setSaving(false);
    }
  };

  const handleDelete = async (tierId: number) => {
    setSaving(true);
    setError("");
    try {
      await api.delete(`/api/v1/channels/me/tiers/${tierId}`);
      setMessage("삭제했습니다. 이미 구독 중인 사람의 혜택은 유지됩니다.");
      await load();
    } catch (err) {
      setError(toErrorMessage(err, "삭제에 실패했습니다."));
    } finally {
      setSaving(false);
    }
  };

  return (
    <main className={styles.container}>
      <header className={styles.header}>
        <p className={styles.eyebrow}>MY CHANNEL</p>
        <h1 className={styles.title}>구독 등록</h1>
        <p className={styles.description}>
          내 채널에서 팔 구독 상품을 등록합니다. 구독자는 여기서 만든 상품 중 하나를 골라 결제하고,
          구독하는 동안 내 글의 블러가 풀립니다.
        </p>
      </header>

      {/* 새 상품 등록 */}
      <section className={styles.card}>
        <h2 className={styles.cardTitle}>새 상품 만들기</h2>

        <div className={styles.field}>
          <label className={styles.label} htmlFor="tier-name">상품 이름</label>
          <input
            id="tier-name"
            className={styles.input}
            type="text"
            maxLength={30}
            placeholder="예: 서포터"
            value={name}
            onChange={(e) => setName(e.target.value)}
          />
        </div>

        <div className={styles.field}>
          <label className={styles.label} htmlFor="tier-price">월 금액(원)</label>
          <input
            id="tier-price"
            className={styles.input}
            type="number"
            min={1}
            step={1}
            placeholder="예: 5000"
            value={price}
            onChange={(e) => setPrice(e.target.value)}
          />
        </div>

        <div className={styles.field}>
          <label className={styles.label} htmlFor="tier-desc">설명 (선택)</label>
          <input
            id="tier-desc"
            className={styles.input}
            type="text"
            maxLength={200}
            placeholder="예: 모든 그림과 후기를 볼 수 있어요"
            value={description}
            onChange={(e) => setDescription(e.target.value)}
          />
        </div>

        <button
          className={styles.primaryButton}
          type="button"
          onClick={handleCreate}
          disabled={saving}
        >
          등록하기
        </button>

        {message && <p className={styles.success}>{message}</p>}
        {error && <p className={styles.error}>{error}</p>}
      </section>

      {/* 등록된 상품 목록 */}
      <section className={styles.card}>
        <h2 className={styles.cardTitle}>등록한 상품 ({tiers.length}개)</h2>

        {loading ? (
          <p className={styles.hint}>불러오는 중...</p>
        ) : tiers.length === 0 ? (
          <p className={styles.hint}>아직 등록한 상품이 없습니다. 위에서 하나 만들어 보세요.</p>
        ) : (
          <ul className={styles.tierList}>
            {tiers.map((tier) => (
              <li className={styles.tierItem} key={tier.id}>
                {editingId === tier.id ? (
                  <div className={styles.editForm}>
                    <p className={styles.editingLabel}>#{tier.id} 수정 중</p>
                    <input
                      className={styles.input}
                      type="text"
                      maxLength={30}
                      value={editName}
                      onChange={(e) => setEditName(e.target.value)}
                    />
                    <input
                      className={styles.input}
                      type="number"
                      min={1}
                      step={1}
                      value={editPrice}
                      onChange={(e) => setEditPrice(e.target.value)}
                    />
                    <input
                      className={styles.input}
                      type="text"
                      maxLength={200}
                      placeholder="설명 (선택)"
                      value={editDescription}
                      onChange={(e) => setEditDescription(e.target.value)}
                    />
                    <div className={styles.rowActions}>
                      <button
                        className={styles.secondaryButton}
                        type="button"
                        onClick={() => setEditingId(null)}
                        disabled={saving}
                      >
                        취소
                      </button>
                      <button
                        className={styles.primaryButton}
                        type="button"
                        onClick={() => handleUpdate(tier.id)}
                        disabled={saving}
                      >
                        저장
                      </button>
                    </div>
                  </div>
                ) : (
                  <>
                    <div className={styles.tierInfo}>
                      {/* 이름은 표시용이고 실제 식별자는 id다. 판매자가 상품을 정확히 지목할 수 있도록 함께 보여준다. */}
                      <p className={styles.tierName}>
                        <span className={styles.tierId}>#{tier.id}</span>
                        {tier.name}
                      </p>
                      <p className={styles.tierPrice}>
                        월 {tier.monthlyPrice.toLocaleString("ko-KR")}원
                      </p>
                      {tier.description && <p className={styles.tierDesc}>{tier.description}</p>}
                    </div>
                    <div className={styles.rowActions}>
                      <button
                        className={styles.secondaryButton}
                        type="button"
                        onClick={() => startEdit(tier)}
                        disabled={saving}
                      >
                        수정
                      </button>
                      <button
                        className={styles.dangerButton}
                        type="button"
                        onClick={() => handleDelete(tier.id)}
                        disabled={saving}
                      >
                        삭제
                      </button>
                    </div>
                  </>
                )}
              </li>
            ))}
          </ul>
        )}
      </section>
    </main>
  );
}
