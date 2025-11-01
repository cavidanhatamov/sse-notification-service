package com.learn.repository;

import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.data.mongodb.repository.Update;
import org.springframework.stereotype.Repository;

import com.learn.model.Notification;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface NotificationRepository
        extends ReactiveMongoRepository<Notification, String>, NotificationRepositoryCustom {

    /**
     * Find active (non-disabled) notifications by user ID
     */
    Flux<Notification> findByUserIdAndDisabledFalse(String userId);

    /**
     * Find notifications by user ID (including disabled)
     */
    Flux<Notification> findByUserId(String userId);

    /**
     * Find notifications by user ID and channel (active only)
     */
    Flux<Notification> findByUserIdAndChannelAndDisabledFalse(String userId, String channel);

    /**
     * Find unread and active notifications by user ID
     */
    Flux<Notification> findByUserIdAndStatus_ReadFalseAndDisabledFalse(String userId);

    /**
     * Find unsent and active notifications by user ID (for SSE initial delivery)
     */
    Flux<Notification> findByUserIdAndStatus_SentFalseAndDisabledFalse(String userId);

    /**
     * Find notifications by template ID (active only)
     */
    Flux<Notification> findByTemplateIdAndDisabledFalse(String templateId);

    /**
     * Find notifications by priority (active only)
     */
    Flux<Notification> findByPriorityAndDisabledFalse(String priority);

    /**
     * Find notifications by source system (active only)
     */
    Flux<Notification> findBySourceSystemAndDisabledFalse(String sourceSystem);

    /**
     * Count unread and active notifications for user
     */
    Mono<Long> countByUserIdAndStatus_ReadFalseAndDisabledFalse(String userId);

    /**
     * Mark a single notification as read
     */
    @Query("{'_id': ?0}")
    @Update("{'$set': {'status.read': true, 'timestamps.readAt': ?1}}")
    Mono<Long> markAsRead(String notificationId, java.time.Instant readAt);

    /**
     * Mark a single notification as sent via SSE
     */
    @Query("{'_id': ?0}")
    @Update("{'$set': {'status.sent': true, 'timestamps.sentAt': ?1}}")
    Mono<Long> markAsSent(String notificationId, java.time.Instant sentAt);

    /**
     * Mark all active notifications as read for a user
     */
    @Query("{'userId': ?0, 'disabled': false}")
    @Update("{'$set': {'status.read': true, 'timestamps.readAt': ?1}}")
    Mono<Long> markAllAsReadByUserId(String userId, java.time.Instant readAt);

    /**
     * Soft delete (disable) all notifications for a user
     */
    @Query("{'userId': ?0}")
    @Update("{'$set': {'disabled': true}}")
    Mono<Long> disableAllByUserId(String userId);

    /**
     * Hard delete disabled notifications for cleanup
     */
    Mono<Long> deleteByUserIdAndDisabledTrue(String userId);

}
