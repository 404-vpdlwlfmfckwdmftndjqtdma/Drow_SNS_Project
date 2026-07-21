package com.canvasflow.subscription.service;

import com.canvasflow.global.exception.CanvasflowException;
import com.canvasflow.global.exception.ErrorCode;
import com.canvasflow.subscription.dto.SubscriptionTierDtos.*;
import com.canvasflow.subscription.entity.SubscriptionTier;
import com.canvasflow.subscription.repository.SubscriptionTierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 구독 등급 관리.
 * 소유권 검증 대신 구조로 차단:
 *  - 생성: channelId를 받지 않고 "로그인 유저의 채널"을 서버가 직접 조회
 *  - 수정/삭제: (tierId + 내 채널) 조건으로 조회 → 남의 tier는 애초에 조회 불가(404)
 */
@Service
@RequiredArgsConstructor
@Transactional
public class SubscriptionTierService {

    private final SubscriptionTierRepository tierRepository;

    /** 구독 상품 생성 - 자동으로 내 채널에 생성됨 */
    public TierResponse create(Long loginUserId, TierCreateRequest request) {
        Long channelId = resolveMyChannelId(loginUserId);

        // 개수 제한은 두지 않는다 - 상품 구성은 판매자가 알아서 설계할 일이다.
        if (tierRepository.existsByChannelIdAndNameAndDeletedFalse(channelId, request.name())) {
            throw new CanvasflowException(ErrorCode.TIER_NAME_DUPLICATED);
        }

        SubscriptionTier tier = SubscriptionTier.builder()
                .channelId(channelId)
                .name(request.name())
                .monthlyPrice(request.monthlyPrice())
                .description(request.description())
                .build();

        return TierResponse.from(tierRepository.save(tier));
    }

    /** 채널의 구독 상품 목록 - 공개 (구독 버튼의 상품 선택 UI용) */
    @Transactional(readOnly = true)
    public List<TierResponse> getTiers(Long channelId) {
        return TierResponse.from(
                tierRepository.findByChannelIdAndDeletedFalseOrderByMonthlyPriceAsc(channelId));
    }

    /** 구독 상품 수정 (이름·금액·설명) */
    public TierResponse update(Long loginUserId, Long tierId, TierUpdateRequest request) {
        SubscriptionTier tier = getMyTierOrThrow(loginUserId, tierId);
        if (tierRepository.existsByChannelIdAndNameAndDeletedFalseAndIdNot(
                tier.getChannelId(), request.name(), tierId)) {
            throw new CanvasflowException(ErrorCode.TIER_NAME_DUPLICATED);
        }
        tier.update(request.name(), request.monthlyPrice(), request.description());
        return TierResponse.from(tier);
    }

    /** 등급 삭제 (소프트 삭제) */
    public void delete(Long loginUserId, Long tierId) {
        SubscriptionTier tier = getMyTierOrThrow(loginUserId, tierId);
        // TODO(팀 논의): 이 등급의 ACTIVE 구독자가 있으면 삭제를 막을지 결정
        tier.delete();
    }

    /**
     * (tierId + 내 채널) 조건 조회.
     * 남의 채널 tier는 조회 자체가 안 되므로 소유권 검증이 불필요.
     * 존재하지 않는 것과 남의 것을 구분하지 않고 404로 응답 (존재 여부 노출 방지에도 유리).
     */
    private SubscriptionTier getMyTierOrThrow(Long loginUserId, Long tierId) {
        Long channelId = resolveMyChannelId(loginUserId);
        return tierRepository.findByIdAndChannelIdAndDeletedFalse(tierId, channelId)
                .orElseThrow(() -> new CanvasflowException(ErrorCode.TIER_NOT_FOUND));
    }

    /**
     * 로그인 유저의 채널 ID.
     *
     * 채널은 별도 개체가 아니라 작가(유저) 자신이다 - 유저 한 명이 곧 채널 하나이므로
     * channelId 는 그 유저의 userId 를 그대로 쓴다. 별도 채널 생성 절차도 없다.
     * (구독 판정도 SubscriptionReader.isSubscribed(viewerId, authorId) 로 같은 규칙을 쓴다.)
     */
    private Long resolveMyChannelId(Long loginUserId) {
        return loginUserId;
    }
}