package com.saidev.nfsystem.repository;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import com.saidev.nfsystem.entity.Notification;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {
    Optional<Notification> findByIdempotencyKey(String idempotencyKey);
    @Modifying
    @Query("""
        UPDATE Notification n
        SET n.status = 'PROCESSING'
        WHERE n.id = :id AND n.status = 'PENDING'
    """)
    int markAsProcessing(@Param("id") UUID id);
}

