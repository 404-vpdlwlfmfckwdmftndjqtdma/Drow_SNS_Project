package com.canvasflow.wallet.repository;

import com.canvasflow.wallet.entity.WalletLedger;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WalletLedgerRepository extends JpaRepository<WalletLedger, Long> {

    /** 내 거래 내역 (최신순) */
    Page<WalletLedger> findByUserIdOrderByIdDesc(Long userId, Pageable pageable);
}
