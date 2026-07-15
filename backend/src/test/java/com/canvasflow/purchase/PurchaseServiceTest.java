package com.canvasflow.purchase;

import com.canvasflow.global.exception.CanvasflowException;
import com.canvasflow.global.exception.ErrorCode;
import com.canvasflow.payment.PaymentGateway;
import com.canvasflow.post.PostReader;
import com.canvasflow.purchase.dto.PurchaseRequest;
import com.canvasflow.purchase.dto.PurchaseResponse;
import com.canvasflow.purchase.entity.PostPurchase;
import com.canvasflow.purchase.repository.PostPurchaseRepository;
import com.canvasflow.purchase.service.PurchaseService;
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
 * 게시물 단건 구매 테스트 (결제 연동 포함)
 *
 * 등장인물:
 *  - 구매자 userId = 1
 *  - 게시불 77번 (작성자 userId = 100, 단건 구매가 3,000원)
 *
 * 핵심 검증 철학:
 *  - 검증 실패 시 결재(confirm)가 "호출조차 되지 않아야" 한다 (돈이 나가면 안 되므로)
 *  - 결재 승인은 반드시 "서버가 조회한 가격"으로 호출되어야 한다 (금액 조작 방어)
 */
@ExtendWith(MockitoExtension.class)
class PurchaseServiceTest {

    private static final Long BUYER_ID = 1L;
    private static final Long AUTHOR_ID = 100L;
    private static final Long POST_ID = 77L;
    private static final BigDecimal PRICE = BigDecimal.valueOf(3000);
    private static final String PAYMENT_KEY = "test-payment-key";
    private static final String ORDER_ID = "order-123";

    @Mock
    PostPurchaseRepository postPurchaseRepository;

    @Mock
    PostReader postReader;

    @Mock
    PaymentGateway paymentGateway;

    @InjectMocks
    PurchaseService purchaseService;

    // ==== 헬퍼 ====

    private PurchaseRequest validRequest() {
        return new PurchaseRequest(PAYMENT_KEY, ORDER_ID);
    }

    /** 구매 가능한 게시물(3,000원)이 존재하는 상태 */
    private void purchasablePost() {
        given(postReader.getPurchaseInfo(POST_ID)).willReturn(Optional.of(new PostReader.PostPurchaseInfo(AUTHOR_ID, PRICE)));
    }

    private void notPurchasedYet() {
        given(postPurchaseRepository.existsByBuyerIdAndPostId(BUYER_ID, POST_ID)).willReturn(false);
    }

    // ==== 1. 정상 구매 ====

    @Test
    @DisplayName("정상 구매: 서버가 조회한 가격으로 결재 승인 후 구매 기록이 저장된다")
    void Normal_Purchase() {
        purchasablePost();
        notPurchasedYet();
        willAnswer(inv -> inv.getArgument(0))
                .given(postPurchaseRepository).save(any(PostPurchase.class));

        PurchaseResponse response = purchaseService.purchase(BUYER_ID, POST_ID, validRequest());

        // 결제가 "서버 가격 3000원"으로 호출 됐는지 - 금액 조작 방어의 핵심 검증
        verify(paymentGateway).confirm(PAYMENT_KEY, ORDER_ID, 3000L);

        // 저장된 구매 기록 내용 검증
        ArgumentCaptor<PostPurchase> captor = ArgumentCaptor.forClass(PostPurchase.class);
        verify(postPurchaseRepository).save(captor.capture());
        PostPurchase saved = captor.getValue();
        assertThat(saved.getBuyerId()).isEqualTo(BUYER_ID);
        assertThat(saved.getPostId()).isEqualTo(POST_ID);
        assertThat(saved.getPrice()).isEqualByComparingTo(PRICE);   // 가격 스냅샷

        assertThat(response.postId()).isEqualTo(POST_ID);
    }

    // ==== 2. 검증 실패 -> 결재가 호출조차 되면 안됨 ====

    @Test
    @DisplayName("없는 게시물: 결제 없이 실패")
    void No_posts() {
        given(postReader.getPurchaseInfo(POST_ID)).willReturn(Optional.empty());
        assertThatThrownBy(() -> purchaseService.purchase(BUYER_ID, POST_ID, validRequest()))
                .isInstanceOf(CanvasflowException.class);
        verifyNoPaymentAndNoSave();
    }

    @Test
    @DisplayName("단건 구매 미개방 글(가격 null): 결제 없이 실패")
    void Unable_purchase_Post() {
        given(postReader.getPurchaseInfo(POST_ID))
                .willReturn(Optional.of(new PostReader.PostPurchaseInfo(AUTHOR_ID, null)));

        assertThatThrownBy(() -> purchaseService.purchase(BUYER_ID, POST_ID, validRequest()))
                .isInstanceOf(CanvasflowException.class);

        verifyNoPaymentAndNoSave();
    }

    @Test
    @DisplayName("본인 글: 결제 없이 실패한다")
    void Cantbuy_yourown() {
        purchasablePost();

        // 구매자 = 작성자
        assertThatThrownBy(() -> purchaseService.purchase(AUTHOR_ID, POST_ID, validRequest()))
                .isInstanceOf(CanvasflowException.class);

        verifyNoPaymentAndNoSave();
    }

    @Test
    @DisplayName("이미 구매한 글: 결제 없이 실패한다 (중복 결제 방지의 핵심)")
    void Duplicate_Purchase() {
        purchasablePost();
        given(postPurchaseRepository.existsByBuyerIdAndPostId(BUYER_ID, POST_ID))
                .willReturn(true);

        assertThatThrownBy(() -> purchaseService.purchase(BUYER_ID, POST_ID, validRequest()))
                .isInstanceOf(CanvasflowException.class);

        verifyNoPaymentAndNoSave();
    }

    @Test
    @DisplayName("결제 정보 누락(paymentKey null): 결제 없이 실패한다")
    void Payment_missing() {
        purchasablePost();
        notPurchasedYet();

        assertThatThrownBy(() ->
                purchaseService.purchase(BUYER_ID, POST_ID, new PurchaseRequest(null, ORDER_ID)))
                .isInstanceOf(CanvasflowException.class);

        verifyNoPaymentAndNoSave();
    }

    // ===== 3. 결제 실패 → 구매 기록이 저장되면 안 됨 =====

    @Test
    @DisplayName("결제 승인 실패: 구매 기록이 저장되지 않는다")
    void Payment_Fail_Saved_Not() {
        purchasablePost();
        notPurchasedYet();
        willThrow(new CanvasflowException(ErrorCode.PAYMENT_CONFIRM_FAILED))
                .given(paymentGateway).confirm(anyString(), anyString(), anyLong());

        assertThatThrownBy(() -> purchaseService.purchase(BUYER_ID, POST_ID, validRequest()))
                .isInstanceOf(CanvasflowException.class);

        verify(postPurchaseRepository, never()).save(any());
    }

    // ===== 4. 동시 요청 최종 방어 =====

    @Test
    @DisplayName("동시 중복 요청: DB 유니크 제약 예외가 ALREADY_PURCHASED로 변환된다")
    void duplex_request() {
        purchasablePost();
        notPurchasedYet();   // 1차 검증은 둘 다 통과한 상황
        willThrow(new DataIntegrityViolationException("uk_buyer_post"))
                .given(postPurchaseRepository).save(any(PostPurchase.class));

        assertThatThrownBy(() -> purchaseService.purchase(BUYER_ID, POST_ID, validRequest()))
                .isInstanceOf(CanvasflowException.class);
        // 이 케이스는 결제가 이미 승인된 뒤이므로 환불 필요 로그가 남는지 콘솔에서 확인
    }

    // ==== 공통 검증 ====
    private void verifyNoPaymentAndNoSave() {
        verify(paymentGateway, never()).confirm(anyString(), anyString(), anyLong());
        verify(postPurchaseRepository, never()).save(any());
    }
}
