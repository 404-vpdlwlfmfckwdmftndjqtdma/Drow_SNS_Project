package com.canvasflow.notification.repository;

import com.canvasflow.notification.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    Page<Notification> findByReceiverIdOrderByCreatedAtDesc(Long receiverId, Pageable pageable);

    long countByReceiverIdAndIsReadFalse(Long receiverId);

    // 선택 삭제용: receiverId까지 같이 걸어서 남의 알림은 id를 알아도 지울 수 없게 한다.
    // 벌크 DELETE라 영속성 컨텍스트를 안 거치므로 @Modifying 필요.
    @Modifying
    @Query("DELETE FROM Notification n WHERE n.id IN :ids AND n.receiverId = :receiverId")
    int deleteByIdInAndReceiverId(@Param("ids") List<Long> ids, @Param("receiverId") Long receiverId);
}
