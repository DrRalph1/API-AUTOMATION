package com.usg.apiAutomation.repositories.postgres.documentation;

import com.usg.apiAutomation.entities.postgres.documentation.NotificationEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<NotificationEntity, String> {

    List<NotificationEntity> findByUserIdOrderByCreatedAtDesc(String userId);

    Page<NotificationEntity> findByUserId(String userId, Pageable pageable);

    @Query("SELECT n FROM NotificationEntity n WHERE n.userId = :userId AND n.isRead = false")
    List<NotificationEntity> findUnreadByUserId(@Param("userId") String userId);

    @Query("SELECT COUNT(n) FROM NotificationEntity n WHERE n.userId = :userId AND n.isRead = false")
    long countUnreadByUserId(@Param("userId") String userId);

    @Modifying
    @Transactional
    @Query("UPDATE NotificationEntity n SET n.isRead = true WHERE n.userId = :userId")
    void markAllAsRead(@Param("userId") String userId);

    @Modifying
    @Transactional
    @Query("DELETE FROM NotificationEntity n WHERE n.createdAt < :expiryDate")
    void deleteOldNotifications(@Param("expiryDate") LocalDateTime expiryDate);

    List<NotificationEntity> findByUserIdAndType(String userId, String type);
}