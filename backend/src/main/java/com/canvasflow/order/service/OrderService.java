package com.canvasflow.order.service;

import com.canvasflow.global.exception.CanvasflowException;
import com.canvasflow.global.exception.ErrorCode;
import com.canvasflow.order.dto.OrderConfirmResponse;
import com.canvasflow.order.dto.OrderCreateResponse;
import com.canvasflow.order.entity.Order;
import com.canvasflow.order.entity.OrderPurpose;
import com.canvasflow.order.repository.OrderRepository;
import com.canvasflow.payment.PaymentGateway;
import com.canvasflow.wallet.WalletCharger;
import com.canvasflow.wallet.WalletReader;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * [주문 조율] 결제 전 주문서를 만들고, 결제가 끝나면 지급까지 마무리한다.
 *
 * 모든 결제의 도착지는 지갑이다. 즉 결제는 "충전"이고, 상품/구독은 지갑에서 차감한다.
 * 덕분에 외부 결제(롤백 불가)가 끼는 구간이 충전 하나로 격리되고,
 * 나머지(차감→지급)는 우리 DB 안이라 한 트랜잭션으로 원자적으로 처리된다.
 */
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final PaymentGateway paymentGateway;   // payment 대문 (승인)
    private final WalletCharger walletCharger;     // wallet 대문 (적립/차감)
    private final WalletReader walletReader;       // wallet 대문 (잔액 조회)

    /**
     * 충전 주문 생성. 결제 전에 서버가 주문번호와 금액을 확정해 둔다.
     * 프론트는 여기서 받은 orderId/amount 로 토스 결제창을 띄운다.
     */
    @Transactional
    public OrderCreateResponse createChargeOrder(Long userId, long amount) {
        if (amount <= 0) {
            throw new CanvasflowException(ErrorCode.WALLET_INVALID_AMOUNT);
        }
        Order order = orderRepository.save(
                new Order(generateOrderId(), userId, OrderPurpose.CHARGE, null, amount));

        return new OrderCreateResponse(order.getOrderId(), order.getAmount(), order.getPurpose());
    }

    /**
     * 결제 확정. 토스 승인 → 지갑 반영까지 한 번에 처리한다.
     *
     * 금액은 프론트가 보낸 값이 아니라 주문에 저장된 값을 쓴다(조작 차단).
     * 이미 지급이 끝난 주문이면 재처리하지 않고 현재 상태를 돌려준다(멱등).
     */
    @Transactional
    public OrderConfirmResponse confirm(Long userId, String orderId, String paymentKey) {
        Order order = orderRepository.findByOrderId(orderId)
                .orElseThrow(() -> new CanvasflowException(ErrorCode.ORDER_NOT_FOUND));

        if (!order.isOwnedBy(userId)) {
            throw new CanvasflowException(ErrorCode.FORBIDDEN);
        }
        // 이미 지급까지 끝난 주문 → 아무것도 다시 하지 않는다
        if (order.isFulfilled()) {
            return new OrderConfirmResponse(
                    order.getOrderId(), order.getPurpose(), order.getAmount(),
                    walletReader.getBalance(userId), true);
        }

        // ① 승인 (payment 가 시도 기록을 독립 트랜잭션으로 남기므로, 이후 롤백돼도 기록은 보존된다)
        Long paymentId = paymentGateway.confirm(paymentKey, orderId, order.getAmount());
        order.linkPayment(paymentId);

        // ② 지갑 반영 - 모든 결제는 일단 충전으로 들어온다
        WalletCharger.Result charged =
                walletCharger.charge(userId, order.getAmount(), order.getId());
        order.linkLedger(charged.ledgerId());

        return new OrderConfirmResponse(
                order.getOrderId(), order.getPurpose(), order.getAmount(),
                charged.balance(), false);
    }

    /** 토스 orderId 규격(6~64자)에 맞춘 주문번호 */
    private String generateOrderId() {
        return "ord_" + UUID.randomUUID().toString().replace("-", "");
    }
}
