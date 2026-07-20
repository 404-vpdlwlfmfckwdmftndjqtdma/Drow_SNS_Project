package com.canvasflow.payment.internal;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

interface PaymentRepository extends JpaRepository<PaymentEntity, Long> {

    boolean existsByPaymentKey(String paymentKey);

    Optional<PaymentEntity> findByPaymentKey(String paymentKey);
}
