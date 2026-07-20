package com.canvasflow.wallet.repository;

import com.canvasflow.wallet.entity.Wallet;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface WalletRepository extends JpaRepository<Wallet, Long> {

    /**
     * 차감/충전용: 잔액 행을 비관적 쓰기 잠금(SELECT ... FOR UPDATE)으로 조회.
     * 같은 사용자의 동시 차감 요청을 줄 세워 이중 사용을 막는다.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select w from Wallet w where w.userId = :userId")
    Optional<Wallet> findByUserIdForUpdate(Long userId);
}
