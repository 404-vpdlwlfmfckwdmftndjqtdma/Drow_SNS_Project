package com.canvasflow.payment.internal;

import org.springframework.data.jpa.repository.JpaRepository;

interface PaymentRepository extends JpaRepository<PaymentEntity, Long> {

    boolean existsByPaymentKey(String paymentKey);
}
