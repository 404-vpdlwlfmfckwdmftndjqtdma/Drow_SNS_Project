package com.canvasflow.order.repository;

import com.canvasflow.order.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    Optional<Order> findByOrderId(String orderId);

    /**
     * 복구용: 결제는 됐는데(paymentId 있음) 지급이 안 된(ledgerId 없음) 주문.
     * 승인 직후 서버가 죽은 경우가 여기 남는다.
     */
    List<Order> findByPaymentIdIsNotNullAndLedgerIdIsNull();
}
