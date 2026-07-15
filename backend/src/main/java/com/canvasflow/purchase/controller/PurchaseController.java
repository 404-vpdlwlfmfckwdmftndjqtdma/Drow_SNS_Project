package com.canvasflow.purchase.controller;

import com.canvasflow.purchase.dto.PurchaseRequest;
import com.canvasflow.purchase.dto.PurchaseResponse;
import com.canvasflow.purchase.service.PurchaseService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class PurchaseController {

    private final PurchaseService purchaseService;

    /**
     * 게시물 단건 구매.
     * postId는 URL로, 가격은 서버가 게시물에서 직접 조회 (body 없음).
     * 클라이언트가 가격을 보내게 하면 조작 가능하므로 절대 body로 받지 말 것.
     */
    @PostMapping("/api/posts/{postId}/purchase")
    public ResponseEntity<PurchaseResponse> purchase(
            /* @AuthenticationPrincipal CustomUserDetails user, */
            @PathVariable Long postId,
            @RequestBody PurchaseRequest request) {
        Long loginUserId = 1L; // TODO: user.getId()
        return ResponseEntity.status(HttpStatus.CREATED).body(purchaseService.purchase(loginUserId, postId, request));
    }

    /** 내 구매 내역 */
    @GetMapping("/api/purchases/me")
    public ResponseEntity<Page<PurchaseResponse>> getMyPurchases(
            /* @AuthenticationPrincipal CustomUserDetails user, */
            Pageable pageable) {
        Long loginUserId = 1L; // TODO
        return ResponseEntity.ok(purchaseService.getMyPurchases(loginUserId, pageable));
    }
}
