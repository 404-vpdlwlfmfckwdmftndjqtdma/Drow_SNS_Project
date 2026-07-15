package com.canvasflow.purchase.service;

import com.canvasflow.global.exception.CanvasflowException;
import com.canvasflow.global.exception.ErrorCode;
import com.canvasflow.payment.PaymentGateway;
import com.canvasflow.post.PostReader;
import com.canvasflow.purchase.dto.PurchaseRequest;
import com.canvasflow.purchase.entity.PostPurchase;
import com.canvasflow.purchase.dto.PurchaseResponse;
import com.canvasflow.purchase.repository.PostPurchaseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PurchaseService {

    private final PostPurchaseRepository postPurchaseRepository;
    private final PostReader postReader;
    private final PaymentGateway paymentGateway;

    /**
     * post 모듈의 공개 창구 (모듈 경계 규칙 때문에 post.repository 직접 참조 금지).
     * post 기본 패키지에 아래 정보를 주는 메서드가 필요합니다:
     *      - 게시물 존재 여부 / authorId / singlePurchasePrice (null이면 단건 구매 불가)
     * TODO: 팀의 post 모듈에 PostReader(가칭) 만들어서 주입
     */

    @Transactional
    public PurchaseResponse purchase(Long buyerId, Long postId, PurchaseRequest request) {
        // 1-1) 게시물 정보 조회 (post 모듈 공개 창구 사용)
         PostReader.PostPurchaseInfo purchaseInfo = postReader.getPurchaseInfo(postId)
                    .orElseThrow(() -> new CanvasflowException(ErrorCode.POST_NOT_FOUND));

        // 1-2) 단건 구매가 열려있는 게시물인가 (가격이 null이면 구독 전용)
        if (purchaseInfo.singlePurchasePrice() == null) {
            throw new CanvasflowException(ErrorCode.PURCHASE_NOT_ALLOWED);
        }

        // 1-3) 본인 글은 구매 불가
        if (buyerId.equals(purchaseInfo.authorId())) {
            throw new CanvasflowException(ErrorCode.PURCHASE_SELF_POST);
        }

        // 1-4) 중복 구매 방지 (1차: 조회로 확인)
        if (postPurchaseRepository.existsByBuyerIdAndPostId(
                buyerId, postId)) {
            throw new CanvasflowException(ErrorCode.ALREADY_PURCHASED);
        }

        // TODO: 실제 결제/포인트 차감이 붙는다면 여기서 처리
        //       (지금은 구매 기록만 저장하는 구조)

        // 2) 검증 통과 후 결재 승인 (금액은 서버가 조회한 게시물 가격)
        if (request.paymentKey() == null || request.orderId() == null) {
            throw  new CanvasflowException(ErrorCode.PAYMENT_REQUIRED);
        }
        paymentGateway.confirm(request.paymentKey(), request.orderId(), purchaseInfo.singlePurchasePrice().longValueExact());

        // 3) 구매 기족 저장
        PostPurchase purchase = PostPurchase.builder()
                .buyerId(buyerId)
                .postId(postId)
                .price(purchaseInfo.singlePurchasePrice())   // 구매 시점 가격 스냅샷
                .build();

        try {
            return PurchaseResponse.from(postPurchaseRepository.save(purchase));
        } catch (DataIntegrityViolationException e) {
            // 2차 방어: 동시에 두 번 눌러 1-4)를 동시에 통과해도
            // (buyer_id, post_id) 유니크 제약이 DB에서 막아줌
            log.error("결재 승인 후 구매 저장 실패 - 환불 필요. paymentKey={}, buyerId={}, postId={}", request.paymentKey(), buyerId, postId);
            throw new CanvasflowException(ErrorCode.ALREADY_PURCHASED);
        }
    }

    /** 내 구매 내역 */
    @Transactional(readOnly = true)
    public Page<PurchaseResponse> getMyPurchases(Long buyerId, Pageable pageable) {
        return postPurchaseRepository
                .findByBuyerIdOrderByCreatedAtDesc(
                        buyerId, pageable)
                .map(PurchaseResponse::from);
    }

}
