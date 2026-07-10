package com.canvasflow.payment.internal;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * 토스 결제 승인 API 호출 담당. secretKey 로 서버-대-서버 인증.
 * 이 호출이 실제 결제를 "확정"시킨다. (프론트 결제창은 인증까지만)
 */
@Component
class TossPaymentClient {

    private static final String CONFIRM_URL = "https://api.tosspayments.com/v1/payments/confirm";

    private final RestClient restClient = RestClient.create();
    private final String authorizationHeader;

    TossPaymentClient(@Value("${toss.secret-key}") String secretKey) {
        // Basic base64(secretKey + ":")  — 비밀번호 없이 시크릿키만
        String encoded = Base64.getEncoder()
                .encodeToString((secretKey + ":").getBytes(StandardCharsets.UTF_8));
        this.authorizationHeader = "Basic " + encoded;
    }

    TossConfirmResponse confirm(String paymentKey, String orderId, Long amount) {
        return restClient.post()
                .uri(CONFIRM_URL)
                .header("Authorization", authorizationHeader)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of(
                        "paymentKey", paymentKey,
                        "orderId", orderId,
                        "amount", amount
                ))
                .retrieve()               // 토스가 4xx(결제 실패 등) 반환 시 예외 발생 → 승인 안 됨
                .body(TossConfirmResponse.class);
    }
}
