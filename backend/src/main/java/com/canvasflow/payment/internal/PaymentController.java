package com.canvasflow.payment.internal;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/payments")
class PaymentController {

    private final PaymentService paymentService;

    PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    /** 프론트 success 페이지가 호출 → 토스 승인 + 저장 */
    @PostMapping("/confirm")
    public PaymentResponse confirm(@RequestBody ConfirmRequest request) {
        return paymentService.confirm(request);
    }
}
