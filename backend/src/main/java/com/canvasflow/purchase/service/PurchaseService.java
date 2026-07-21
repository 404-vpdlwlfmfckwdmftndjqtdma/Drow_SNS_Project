package com.canvasflow.purchase.service;

import com.canvasflow.global.exception.CanvasflowException;
import com.canvasflow.global.exception.ErrorCode;
import com.canvasflow.post.PostReader;
import com.canvasflow.purchase.dto.PurchaseRequest;
import com.canvasflow.purchase.dto.PurchaseResponse;
import com.canvasflow.purchase.dto.ProductOfferResponse;
import com.canvasflow.purchase.entity.PurchaseItem;
import com.canvasflow.purchase.repository.PurchaseItemRepository;
import com.canvasflow.wallet.WalletCharger;
import com.canvasflow.wallet.WalletReader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * [단건 구매] 글에 걸린 기능(텍스트 블러 / 이미지 블러 ...)의 잠금을 개별로 사는 도메인.
 *
 * 결제 수단은 오직 지갑이다. 외부 결제(토스)는 order 모듈의 "충전"에서만 일어나고,
 * 여기서는 이미 충전된 잔액을 차감하기만 한다.
 * 덕분에 이 구간은 전부 우리 DB 안이라 한 트랜잭션으로 원자 처리되고,
 * 실패하면 차감도 같이 롤백된다(외부 결제였다면 환불 처리가 필요했을 자리).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PurchaseService {

    private final PurchaseItemRepository purchaseItemRepository;
    private final PostReader postReader;          // post 모듈 창구 (가격표 조회)
    private final WalletCharger walletCharger;    // wallet 창구 (차감)
    private final WalletReader walletReader;      // wallet 창구 (잔액 조회)

    /**
     * 결제 화면 데이터: 이 글에서 살 수 있는 상품 목록 + 각 구매 여부 + 내 잔액.
     * 화면이 "보유 토큰 / 결제할 금액 / 부족분"을 한 번에 그릴 수 있도록 한 요청으로 합쳤다.
     */
    @Transactional(readOnly = true)
    public ProductOfferResponse getOffers(Long viewerId, Long postId) {
        List<PostReader.ProductInfo> products = postReader.getProducts(postId);
        long balance = viewerId == null ? 0L : walletReader.getBalance(viewerId);

        List<ProductOfferResponse.Offer> offers = products.stream()
                .map(product -> new ProductOfferResponse.Offer(
                        product.capability(),
                        product.price(),
                        viewerId != null && purchaseItemRepository.existsByBuyerIdAndPostIdAndCapability(
                                viewerId, postId, product.capability())
                ))
                .toList();

        return new ProductOfferResponse(postId, balance, offers);
    }

    /**
     * 단건 구매. 가격은 서버가 가격표에서 조회하고, 지갑에서 차감한 뒤 권한을 부여한다.
     * 잔액이 모자라면 WALLET_INSUFFICIENT_BALANCE - 화면은 이걸 받아 충전을 유도한다.
     */
    @Transactional
    public PurchaseResponse purchase(Long buyerId, Long postId, PurchaseRequest request) {
        String capability = request.capability();

        // 1) 가격표 확인 (없거나 판매 중지면 구매 불가). 가격은 반드시 서버 조회 값을 쓴다.
        PostReader.ProductInfo product = postReader.getProduct(postId, capability)
                .orElseThrow(() -> new CanvasflowException(ErrorCode.PURCHASE_NOT_ALLOWED));

        // 2) 본인 글은 구매 불가 (작성자는 어차피 전부 열람 가능)
        if (buyerId.equals(product.authorId())) {
            throw new CanvasflowException(ErrorCode.PURCHASE_SELF_POST);
        }

        // 3) 중복 구매 방지 (1차: 조회). 동시 요청은 아래 유니크 제약이 2차로 막는다.
        if (purchaseItemRepository.existsByBuyerIdAndPostIdAndCapability(buyerId, postId, capability)) {
            throw new CanvasflowException(ErrorCode.ALREADY_PURCHASED);
        }

        // 4) 지갑 차감 - 잔액이 모자라면 여기서 예외가 나고 아무것도 저장되지 않는다
        BigDecimal price = product.price();
        walletCharger.useForPurchase(buyerId, price.longValueExact(), postId);

        // 5) 권한 부여 + 구매 기록 (가격 스냅샷 포함)
        try {
            PurchaseItem saved = purchaseItemRepository.save(
                    new PurchaseItem(buyerId, postId, capability, price));
            return PurchaseResponse.from(saved);
        } catch (DataIntegrityViolationException e) {
            // 동시에 두 번 눌러 3)을 함께 통과한 경우. 차감까지 같은 트랜잭션이라 함께 롤백된다.
            log.warn("중복 구매 동시 요청 차단 - buyerId={}, postId={}, capability={}", buyerId, postId, capability);
            throw new CanvasflowException(ErrorCode.ALREADY_PURCHASED);
        }
    }

    /** 내 구매 내역 */
    @Transactional(readOnly = true)
    public Page<PurchaseResponse> getMyPurchases(Long buyerId, Pageable pageable) {
        return purchaseItemRepository
                .findByBuyerIdOrderByCreatedAtDesc(buyerId, pageable)
                .map(PurchaseResponse::from);
    }
}
