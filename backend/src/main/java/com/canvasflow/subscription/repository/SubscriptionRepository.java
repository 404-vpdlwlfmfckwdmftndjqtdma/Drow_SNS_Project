package com.canvasflow.subscription.repository;

import com.canvasflow.subscription.entity.Subscription;
import com.canvasflow.subscription.entity.SubscriptionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    /** 열람 판정의 핵심 조회 (유니크 제약이 인덱스 역할) */
    Optional<Subscription> findBySubscriberIdAndChannelId(Long subscriberId, Long channelId);

    /** 내가 구독 중인 채널 목록 */
    List<Subscription> findBySubscriberIdAndStatus(Long subscriberId, SubscriptionStatus status);

    /** 채널의 구독자 목록 */
    List<Subscription> findByChannelIdAndStatus(Long channelId, SubscriptionStatus status);

    /** 채널 구독자 수 */
    long countByChannelIdAndStatus(Long channelId, SubscriptionStatus status);

    /**
     * 다른 메서드들은 이름만으로 Spring Data JPA가 쿼리를 만들어주지만, fetch join은 메서드 이름 규칙으로 표현할 수 없어서 JPQL을 직접 썼음
     * 구독 목록을 DTO로 바꿀 때 tier.getName()을 읽는데, fetch join 없이 가면 구독 20개에 tier 쿼리가 20번 나가는 N+1이 생기니까 이렇게 한 번에 당겨오는 것.
     * 그리고 페이징이 붙으면 count 쿼리에서는 fetch join이 안 되기 때문에 countQuery를 따로 지정해줌
     * @param subscriberId
     * @param status
     * @param pageable
     * @return
     */
    @Query(value = "select s from Subscription s left join fetch s.tier " +
            "where s.subscriberId = :subscriberId and s.status = :status",
            countQuery = "select count(s) from Subscription s " +
                    "where s.subscriberId = :subscriberId and s.status = :status")
    Page<Subscription> findWithTierBySubscriberIdAndStatus(
            @Param("subscriberId") Long subscriberId,
            @Param("status") SubscriptionStatus status,
            Pageable pageable);
}
