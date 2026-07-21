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

    private static final int MAX_TIERS_PER_CHANNEL = 5;

    private final SubscriptionTierRepository tierRepository;

    /** 등급 생성 - 자동으로 내 채널에 생성됨 */
    public TierResponse create(Long loginUserId, TierCreateRequest request) {
        Long channelId = resolveMyChannelId(loginUserId);

        if (tierRepository.existsByChannelIdAndLevelAndDeletedFalse(channelId, request.level())) {
            throw new CanvasflowException(ErrorCode.TIER_LEVEL_DUPLICATED);
        }
        if (tierRepository.countByChannelIdAndDeletedFalse(channelId) >= MAX_TIERS_PER_CHANNEL) {
            throw new CanvasflowException(ErrorCode.TIER_LIMIT_EXCEEDED);
        }

        SubscriptionTier tier = SubscriptionTier.builder()
                .channelId(channelId)
                .name(request.name())
                .level(request.level())
                .monthlyPrice(request.monthlyPrice())
                .description(request.description())
                .build();

        return TierResponse.from(tierRepository.save(tier));
    }

    /** 채널의 등급 목록 - 공개 (채널 페이지의 구독 유도 UI용) */
    @Transactional(readOnly = true)
    public List<TierResponse> getTiers(Long channelId) {
        return TierResponse.from(
                tierRepository.findByChannelIdAndDeletedFalseOrderByLevelAsc(channelId));
    }

    /** 등급 수정 - level 변경 불가 */
    public TierResponse update(Long loginUserId, Long tierId, TierUpdateRequest request) {
        SubscriptionTier tier = getMyTierOrThrow(loginUserId, tierId);
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

    private Long resolveMyChannelId(Long loginUserId) {
        return loginUserId;     // ChannelId = userId
    }
}