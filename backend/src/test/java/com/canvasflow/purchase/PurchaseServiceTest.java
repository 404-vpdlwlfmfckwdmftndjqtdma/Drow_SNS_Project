package com.canvasflow.purchase;

import com.canvasflow.global.exception.CanvasflowException;
import com.canvasflow.global.exception.ErrorCode;
import com.canvasflow.post.PostReader;
import com.canvasflow.purchase.dto.PurchaseRequest;
import com.canvasflow.purchase.dto.PurchaseResponse;
import com.canvasflow.purchase.entity.PurchaseItem;
import com.canvasflow.purchase.repository.PurchaseItemRepository;
import com.canvasflow.purchase.service.PurchaseService;
import com.canvasflow.wallet.WalletCharger;
import com.canvasflow.wallet.WalletReader;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * 게시물 기능 단건 구매 테스트 (지갑 차감 방식)
 *
 * 등장인물:
 *  - 구매자 userId = 1
 *  - 게시물 77번 (작성자 userId = 100, imageBlur 해제가 3,000원)
 *
 * 핵심 검증 철학:
 *  - 검증 실패 시 지갑 차감이 "호출조차 되지 않아야" 한다 (돈이 빠지면 안 되므로)
 *  - 차감은 반드시 "서버가 가격표에서 조회한 금액"으로 호출되어야 한다 (금액 조작 방어)
 */
@ExtendWith(MockitoExtension.class)
class PurchaseServiceTest {

    private static final Long BUYER_ID = 1L;
    private static final Long AUTHOR_ID = 100L;
    private static final Long POST_ID = 77L;
    private static final String CAPABILITY = "imageBlur";
    private static final BigDecimal PRICE = BigDecimal.valueOf(3000);

    @Mock
    PurchaseItemRepository purchaseItemRepository;

    @Mock
    PostReader postReader;

    @Mock
    WalletCharger walletCharger;

    @Mock
    WalletReader walletReader;

    @InjectMocks
    PurchaseService purchaseService;

    // ==== 헬퍼 ====

    private PurchaseRequest validRequest() {
        return new PurchaseRequest(CAPABILITY);
    }

    /** 3,000원짜리 imageBlur 상품이 판매 중인 상태 */
    private void onSaleProduct() {
        given(postReader.getProduct(POST_ID, CAPABILITY))
                .willReturn(Optional.of(new PostReader.ProductInfo(POST_ID, AUTHOR_ID, CAPABILITY, PRICE)));
    }

    private void notPurchasedYet() {
        given(purchaseItemRepository.existsByBuyerIdAndPostIdAndCapability(BUYER_ID, POST_ID, CAPABILITY))
                .willReturn(false);
    }

    // ==== 1. 정상 구매 ====

    @Test
    @DisplayName("정상 구매: 서버가 조회한 가격으로 지갑을 차감하고 권한을 저장한다")
    void 정상_구매() {
        onSaleProduct();
        notPurchasedYet();
        willAnswer(inv -> inv.getArgument(0))
                .given(purchaseItemRepository).save(any(PurchaseItem.class));

        PurchaseResponse response = purchaseService.purchase(BUYER_ID, POST_ID, validRequest());

        // 차감이 "서버 가격 3000원"으로 호출됐는지 - 금액 조작 방어의 핵심 검증
        verify(walletCharger).useForPurchase(BUYER_ID, 3000L, POST_ID);

        ArgumentCaptor<PurchaseItem> captor = ArgumentCaptor.forClass(PurchaseItem.class);
        verify(purchaseItemRepository).save(captor.capture());
        PurchaseItem saved = captor.getValue();
        assertThat(saved.getBuyerId()).isEqualTo(BUYER_ID);
        assertThat(saved.getPostId()).isEqualTo(POST_ID);
        assertThat(saved.getCapability()).isEqualTo(CAPABILITY);
        assertThat(saved.getPrice()).isEqualByComparingTo(PRICE);   // 가격 스냅샷

        assertThat(response.postId()).isEqualTo(POST_ID);
        assertThat(response.capability()).isEqualTo(CAPABILITY);
    }

    // ==== 2. 검증 실패 -> 차감이 호출조차 되면 안 됨 ====

    @Test
    @DisplayName("없는 글 / 판매 중지 상품: 차감 없이 실패")
    void 판매하지_않는_상품() {
        given(postReader.getProduct(POST_ID, CAPABILITY)).willReturn(Optional.empty());

        assertThatThrownBy(() -> purchaseService.purchase(BUYER_ID, POST_ID, validRequest()))
                .isInstanceOf(CanvasflowException.class);

        verifyNoChargeAndNoSave();
    }

    @Test
    @DisplayName("본인 글: 차감 없이 실패한다")
    void 본인_글은_구매_불가() {
        onSaleProduct();

        // 구매자 = 작성자
        assertThatThrownBy(() -> purchaseService.purchase(AUTHOR_ID, POST_ID, validRequest()))
                .isInstanceOf(CanvasflowException.class);

        verifyNoChargeAndNoSave();
    }

    @Test
    @DisplayName("이미 산 기능: 차감 없이 실패한다 (중복 결제 방지의 핵심)")
    void 중복_구매_방지() {
        onSaleProduct();
        given(purchaseItemRepository.existsByBuyerIdAndPostIdAndCapability(BUYER_ID, POST_ID, CAPABILITY))
                .willReturn(true);

        assertThatThrownBy(() -> purchaseService.purchase(BUYER_ID, POST_ID, validRequest()))
                .isInstanceOf(CanvasflowException.class);

        verifyNoChargeAndNoSave();
    }

    // ===== 3. 잔액 부족 → 권한이 저장되면 안 됨 =====

    @Test
    @DisplayName("잔액 부족: 권한이 저장되지 않는다")
    void 잔액_부족() {
        onSaleProduct();
        notPurchasedYet();
        willThrow(new CanvasflowException(ErrorCode.WALLET_INSUFFICIENT_BALANCE))
                .given(walletCharger).useForPurchase(anyLong(), anyLong(), anyLong());

        assertThatThrownBy(() -> purchaseService.purchase(BUYER_ID, POST_ID, validRequest()))
                .isInstanceOf(CanvasflowException.class);

        verify(purchaseItemRepository, never()).save(any());
    }

    // ===== 4. 동시 요청 최종 방어 =====

    @Test
    @DisplayName("동시 중복 요청: DB 유니크 제약 예외가 ALREADY_PURCHASED로 변환된다")
    void 동시_중복_요청() {
        onSaleProduct();
        notPurchasedYet();   // 1차 검증은 둘 다 통과한 상황
        willThrow(new DataIntegrityViolationException("uk_buyer_post_capability"))
                .given(purchaseItemRepository).save(any(PurchaseItem.class));

        // 차감까지 같은 트랜잭션이라 예외와 함께 롤백된다 (외부 결제였다면 환불이 필요했을 자리)
        assertThatThrownBy(() -> purchaseService.purchase(BUYER_ID, POST_ID, validRequest()))
                .isInstanceOf(CanvasflowException.class)
                .extracting(e -> ((CanvasflowException) e).getErrorCode())
                .isEqualTo(ErrorCode.ALREADY_PURCHASED);
    }

    // ===== 5. 결제 화면 데이터 =====

    @Test
    @DisplayName("결제 화면: 상품 목록에 내 잔액과 구매 여부가 함께 내려간다")
    void 결제_화면_데이터() {
        given(postReader.getProducts(POST_ID)).willReturn(java.util.List.of(
                new PostReader.ProductInfo(POST_ID, AUTHOR_ID, CAPABILITY, PRICE)));
        given(walletReader.getBalance(BUYER_ID)).willReturn(5000L);
        given(purchaseItemRepository.existsByBuyerIdAndPostIdAndCapability(BUYER_ID, POST_ID, CAPABILITY))
                .willReturn(false);

        var offers = purchaseService.getOffers(BUYER_ID, POST_ID);

        assertThat(offers.balance()).isEqualTo(5000L);
        assertThat(offers.offers()).hasSize(1);
        assertThat(offers.offers().get(0).capability()).isEqualTo(CAPABILITY);
        assertThat(offers.offers().get(0).price()).isEqualByComparingTo(PRICE);
        assertThat(offers.offers().get(0).purchased()).isFalse();
    }

    // ==== 공통 검증 ====
    private void verifyNoChargeAndNoSave() {
        verify(walletCharger, never()).useForPurchase(anyLong(), anyLong(), anyLong());
        verify(purchaseItemRepository, never()).save(any());
    }
}
