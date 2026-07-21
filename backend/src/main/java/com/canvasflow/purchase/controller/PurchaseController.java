package com.canvasflow.purchase.controller;

import com.canvasflow.global.exception.CanvasflowException;
import com.canvasflow.global.exception.ErrorCode;
import com.canvasflow.global.response.ApiResponse;
import com.canvasflow.global.security.AuthMember;
import com.canvasflow.purchase.dto.ProductOfferResponse;
import com.canvasflow.purchase.dto.PurchaseRequest;
import com.canvasflow.purchase.dto.PurchaseResponse;
import com.canvasflow.purchase.service.PurchaseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class PurchaseController {

    private final PurchaseService purchaseService;

    /**
     * 결제 화면 데이터: 이 글에서 살 수 있는 상품 목록 + 구매 여부 + 내 지갑 잔액.
     * 비로그인도 상품/가격은 볼 수 있고, 이때 잔액은 0으로 내려간다.
     */
    @GetMapping("/api/v1/posts/{postId}/products")
    public ResponseEntity<ApiResponse<ProductOfferResponse>> getProducts(
            @AuthenticationPrincipal AuthMember authMember,
            @PathVariable Long postId) {
        Long viewerId = authMember != null ? authMember.userId() : null;
        return ResponseEntity.ok(ApiResponse.ok(purchaseService.getOffers(viewerId, postId)));
    }

    /**
     * 게시물 기능 단건 구매.
     * postId·capability만 받고 가격은 서버가 가격표에서 조회한다(조작 차단).
     * 결제는 지갑 차감이라 결제 수단 정보를 받지 않는다 - 잔액이 모자라면 402.
     */
    @PostMapping("/api/v1/posts/{postId}/purchase")
    public ResponseEntity<ApiResponse<PurchaseResponse>> purchase(
            @AuthenticationPrincipal AuthMember authMember,
            @PathVariable Long postId,
            @Valid @RequestBody PurchaseRequest request) {
        PurchaseResponse response = purchaseService.purchase(requireLogin(authMember), postId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(response));
    }

    /** 내 구매 내역 */
    @GetMapping("/api/v1/purchases/me")
    public ResponseEntity<ApiResponse<Page<PurchaseResponse>>> getMyPurchases(
            @AuthenticationPrincipal AuthMember authMember,
            Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(purchaseService.getMyPurchases(requireLogin(authMember), pageable)));
    }

    // 구매는 결제·권한이 걸린 행위라 반드시 로그인 사용자 기준.
    // SecurityConfig가 아직 permitAll이라 필터를 통과해도 authMember가 null로 들어올 수 있어 여기서 막는다.
    private Long requireLogin(AuthMember authMember) {
        if (authMember == null) {
            throw new CanvasflowException(ErrorCode.UNAUTHORIZED);
        }
        return authMember.userId();
    }
}
